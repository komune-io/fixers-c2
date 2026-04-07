package s2.spring.automate.ssm.persister

import f2.dsl.fnc.operators.batchFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.InitTransitionAppliedContext
import s2.automate.core.context.TransitionAppliedContext
import s2.automate.core.context.asBatch
import s2.automate.core.persist.AutomatePersister
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2Iteration
import s2.dsl.automate.model.WithS2State
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.tx.dsl.features.ssm.SsmSessionPerformActionCommand
import ssm.tx.dsl.features.ssm.SsmSessionStartCommand
import ssm.tx.dsl.features.ssm.SsmTxSessionPerformActionFunction
import ssm.tx.dsl.features.ssm.SsmTxSessionStartFunction
import tools.jackson.databind.ObjectMapper

class SsmAutomatePersister<STATE, ID, ENTITY, EVENT>(
	internal var ssmSessionStartFunction: SsmTxSessionStartFunction,
	internal var ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction,
	internal var ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,

	internal var chaincodeUri: ChaincodeUri,
	internal var entityType: Class<ENTITY>,
	internal var agentSigner: Agent,
	internal var objectMapper: ObjectMapper,
	internal var batch: S2BatchProperties,
	internal var permisive: Boolean = false
) : AutomatePersister<STATE, ID, ENTITY, EVENT, S2Automate> where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID> {

	override suspend fun load(automateContexts: AutomateContext<S2Automate>, id: ID & Any): ENTITY? {
		return load(automateContexts, flowOf(id)).firstOrNull()
	}

	override suspend fun load(automateContexts: AutomateContext<S2Automate>, ids: Flow<ID & Any>): Flow<ENTITY> {
		return ids.map {
			GetAutomateSessionQuery(automateContext = automateContexts, sessionId = it.toString())
		}.let {
			getSessionForAutomate(it)
		}.map { session ->
			val lastTransaction = session.logs.maxByOrNull { transaction ->
				transaction.state.iteration
			}
			objectMapper.readValue(lastTransaction!!.state.public as String, entityType)
		}
	}

	override suspend fun persistInit(
		transitionContexts: Flow<InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
	): Flow<EVENT> {
		return persistInternal(transitionContexts).map { it.second }
	}

	private fun persistInternal(
		transitionContexts: Flow<InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
	): Flow<Pair<ENTITY, EVENT>> = flow {
		val collectedContexts = transitionContexts.toList()

		val ssmStartCommands = collectedContexts.map { transitionContext ->
			val entity = transitionContext.entity
			val automate = transitionContext.automateContext.automate

			SsmSessionStartCommand(
				session = SsmSession(
					ssm = automate.name,
					session = entity.s2Id().toString(),
					roles = mapOf(agentSigner.name to automate.transitions[0].role.name),
					public = objectMapper.writeValueAsString(entity),
					private = mapOf()
				),
				signerName = agentSigner.name,
				chaincodeUri = chaincodeUri
			)
		}

		ssmSessionStartFunction.invoke(ssmStartCommands.asFlow()).collect()

		collectedContexts.forEach { transitionContext ->
			emit(transitionContext.entity to transitionContext.event)
		}
	}

	private fun getIterations(
		query: Flow<GetSessionQuery<STATE, ID, ENTITY, EVENT>>
	): Flow<GetSessionResult<STATE, ID, ENTITY, EVENT>> {
		return if(entityType.isAssignableFrom(WithS2Iteration::class.java)) {
			query.map {
				val entity = it.transitionContext.entity as WithS2Iteration
				val iteration = entity.s2Iteration()
				GetSessionResult(
					transitionContext = it.transitionContext,
					sessionId = it.sessionId,
					iteration = iteration
				)
			}
		} else {
			query.batchFlow(batch.asBatch()) { list ->
				val bySession = list.associateBy { it.sessionId }
				getSessions(list).map { session ->
					val it = bySession[session.sessionName]!!
					val iteration = session.logs.maxOfOrNull { it.state.iteration }

					GetSessionResult(
						transitionContext = it.transitionContext,
						sessionId = it.sessionId,
						iteration = iteration ?: 0
					)
				}
			}
		}
	}

	private suspend fun getSessions(
		queries: List<GetSessionQuery<STATE, ID, ENTITY, EVENT>>,
	): Flow<SsmGetSessionLogsQueryResult> = queries.map { query ->
		SsmGetSessionLogsQuery(
			sessionName = query.sessionId,
			chaincodeUri = chaincodeUri,
			ssmName = query.transitionContext.automateContext.automate.name,
		)
	}.let {
		ssmGetSessionLogsQueryFunction.invoke(it.asFlow())
	}

	private suspend fun getSessionForAutomate(
		queries: Flow<GetAutomateSessionQuery>,
	): Flow<SsmGetSessionLogsQueryResult> = queries.map { query ->
		SsmGetSessionLogsQuery(
			sessionName = query.sessionId,
			chaincodeUri = chaincodeUri,
			ssmName = query.automateContext.automate.name,
		)
	}.let {
		ssmGetSessionLogsQueryFunction.invoke(it)
	}

	override suspend fun persist(
		transitionContexts: Flow<TransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
	): Flow<EVENT> = flow {
		val collectedContexts = transitionContexts.toList()

		val ssmCommands = collectedContexts.map { transitionContext ->
			val sessionName = transitionContext.entity.s2Id().toString()
			GetSessionQuery(transitionContext, sessionName)
		}.let {
			getIterations(it.asFlow())
		}.map { (transitionContext, _,iteration) ->

			val entity = transitionContext.entity

			val withEventAsAction = transitionContext.automateContext.automate.withResultAsAction
			val action = transitionContext.event?.takeIf { withEventAsAction } ?: transitionContext.msg
			SsmSessionPerformActionCommand(
				action = action::class.simpleName!!,
				context = SsmContext(
					session = entity.s2Id().toString(),
					public = objectMapper.writeValueAsString(entity),
					private = mapOf(),
					iteration = iteration,
				),
				signerName = agentSigner.name,
				chaincodeUri = chaincodeUri
			)
		}

		ssmSessionPerformActionFunction.invoke(ssmCommands).collect()

		collectedContexts.forEach { e ->
			emit(e.event)
		}
	}
}

data class GetSessionQuery<STATE, ID, ENTITY, EVENT>(
	val transitionContext: TransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>,
	val sessionId: SessionName
) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>

data class GetAutomateSessionQuery(
	val automateContext: AutomateContext<S2Automate>,
	val sessionId: SessionName
)

data class GetSessionResult<STATE, ID, ENTITY, EVENT>(
	val transitionContext: TransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>,
	val sessionId: SessionName,
	val iteration: Int
) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>
