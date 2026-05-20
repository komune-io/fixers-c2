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
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.s2error
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
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunctionV2
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunctionV2
import ssm.sdk.core.command.SsmPerformCommandV2
import ssm.sdk.core.command.SsmStartCommandV2
import ssm.sdk.dsl.CommandOutcome
import ssm.tx.dsl.features.ssm.SsmSessionPerformActionCommand
import ssm.tx.dsl.features.ssm.SsmSessionStartCommand
import ssm.tx.dsl.features.ssm.SsmTxSessionPerformActionFunction
import ssm.tx.dsl.features.ssm.SsmTxSessionStartFunction
import tools.jackson.databind.ObjectMapper

class SsmAutomatePersister<STATE, ID, ENTITY, EVENT>(
	internal var ssmSessionStartFunction: SsmTxSessionStartFunction,
	internal var ssmSessionPerformActionFunction: SsmTxSessionPerformActionFunction,
	internal var ssmSessionStartFunctionV2: SsmTxSessionStartFunctionV2,
	internal var ssmSessionPerformActionFunctionV2: SsmTxSessionPerformActionFunctionV2,
	internal var ssmGetSessionLogsQueryFunction: SsmGetSessionLogsQueryFunction,

	internal var chaincodeUri: ChaincodeUri,
	internal var entityType: Class<ENTITY>,
	internal var agentSigner: Agent,
	internal var objectMapper: ObjectMapper,
	internal var batch: S2BatchProperties,
	internal var permissive: Boolean = false
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
			} ?: throw IllegalStateException("No logs found for session ${session.sessionName}")
			objectMapper.readValue(lastTransaction.state.public.toString(), entityType)
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
					roles = mapOf(agentSigner.name to automate.transitions.first().role.name),
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

	override suspend fun persistInitWithOutcomes(
		transitionContexts: Flow<InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
	): Flow<PersistOutcome<EVENT>> = flow {
		val collectedContexts = transitionContexts.toList()

		val v2Commands = collectedContexts.map { ctx ->
			val entity = ctx.entity
			val automate = ctx.automateContext.automate
			SsmStartCommandV2(
				commandId = "start:${entity.s2Id()}",
				session = SsmSession(
					ssm = automate.name,
					session = entity.s2Id().toString(),
					roles = mapOf(agentSigner.name to automate.transitions.first().role.name),
					public = objectMapper.writeValueAsString(entity),
					private = mapOf(),
				),
				signerName = agentSigner.name,
				chaincodeUri = chaincodeUri,
			)
		}

		val outcomes = ssmSessionStartFunctionV2.invoke(v2Commands.asFlow()).toList()
		val byId = outcomes.associateBy { it.commandId }

		collectedContexts.forEach { ctx ->
			val cid = "start:${ctx.entity.s2Id()}"
			emit(toPersistOutcome(cid, ctx.event, byId[cid]))
		}
	}

	private fun getIterations(
		query: Flow<GetSessionQuery<STATE, ID, ENTITY, EVENT>>
	): Flow<GetSessionResult<STATE, ID, ENTITY, EVENT>> {
		return if (WithS2Iteration::class.java.isAssignableFrom(entityType)) {
			query.map {
				val entity = it.transitionContext.entity as WithS2Iteration
				val iteration = entity.s2Iteration()
				GetSessionResult(
					transitionContext = it.transitionContext,
					sessionId = it.sessionId,
					iteration = iteration,
				)
			}
		} else {
			query.batchFlow(batch.asBatch()) { list ->
				val bySession = list.associateBy { it.sessionId }
				val sessions = getSessions(list).toList()
				val foundIds = sessions.map { it.sessionName }.toSet()
				val happy = sessions.map { session ->
					val context = bySession.getValue(session.sessionName)
					val iteration = session.logs.maxOfOrNull { it.state.iteration } ?: -1
					if (iteration < 0) {
						GetSessionResult(
							transitionContext = context.transitionContext,
							sessionId = context.sessionId,
							iteration = 0,
							failure = PersistOutcome.Rejected(
								commandId = commandIdFor(context),
								error = s2error(
									code = "NO_LOGS",
									description = "No logs for session ${session.sessionName}",
								),
							),
						)
					} else {
						GetSessionResult(context.transitionContext, context.sessionId, iteration)
					}
				}
				val missing = list.filterNot { it.sessionId in foundIds }.map { miss ->
					GetSessionResult(
						transitionContext = miss.transitionContext,
						sessionId = miss.sessionId,
						iteration = 0,
						failure = PersistOutcome.Rejected(
							commandId = commandIdFor(miss),
							error = s2error(
								code = "SESSION_NOT_FOUND",
								description = "Session ${miss.sessionId} not on chaincode",
							),
						),
					)
				}
				(happy + missing).asFlow()
			}
		}
	}

	private fun commandIdFor(query: GetSessionQuery<STATE, ID, ENTITY, EVENT>): String {
		return "${query.sessionId}:lookup"
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
		}.map { (transitionContext, _, iteration) ->

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

	override suspend fun persistWithOutcomes(
		transitionContexts: Flow<TransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
	): Flow<PersistOutcome<EVENT>> = flow {
		val collectedContexts = transitionContexts.toList()

		val sessionResults: List<GetSessionResult<STATE, ID, ENTITY, EVENT>> = collectedContexts.map {
			val sessionName = it.entity.s2Id().toString()
			GetSessionQuery(it, sessionName)
		}.let { getIterations(it.asFlow()) }.toList()

		sessionResults.filter { it.failure != null }.forEach { failed ->
			@Suppress("UNCHECKED_CAST")
			emit(failed.failure!! as PersistOutcome<EVENT>)
		}

		val good = sessionResults.filter { it.failure == null }
		val v2Commands = good.map { sr ->
			val entity = sr.transitionContext.entity
			val withEventAsAction = sr.transitionContext.automateContext.automate.withResultAsAction
			val action = sr.transitionContext.event?.takeIf { withEventAsAction } ?: sr.transitionContext.msg
			SsmPerformCommandV2(
				commandId = "${sr.sessionId}:${sr.iteration + 1}",
				action = action::class.simpleName!!,
				context = SsmContext(
					session = entity.s2Id().toString(),
					public = objectMapper.writeValueAsString(entity),
					private = mapOf(),
					iteration = sr.iteration,
				),
				signerName = agentSigner.name,
				chaincodeUri = chaincodeUri,
			)
		}

		val outcomes = ssmSessionPerformActionFunctionV2.invoke(v2Commands.asFlow()).toList()
		val byId = outcomes.associateBy { it.commandId }

		good.forEach { sr ->
			val cid = "${sr.sessionId}:${sr.iteration + 1}"
			@Suppress("UNCHECKED_CAST")
			emit(toPersistOutcome(cid, sr.transitionContext.event as EVENT, byId[cid]))
		}
	}

	private fun <E> toPersistOutcome(commandId: String, event: E, outcome: CommandOutcome?): PersistOutcome<E> {
		if (outcome == null) {
			return PersistOutcome.Indeterminate(
				commandId = commandId,
				error = s2error(
					code = "MISSING_OUTCOME",
					description = "No CommandOutcome returned for $commandId",
				),
			)
		}
		return when (outcome.outcome) {
			"Committed" -> PersistOutcome.Success(
				commandId = commandId,
				event = event,
				transactionId = outcome.transactionId.orEmpty(),
				blockNumber = outcome.blockNumber ?: 0L,
			)
			"Rejected" -> PersistOutcome.Rejected(
				commandId = commandId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			"Transient" -> PersistOutcome.Transient(
				commandId = commandId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			"Indeterminate" -> PersistOutcome.Indeterminate(
				commandId = commandId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			"Conflict" -> PersistOutcome.Conflict(
				commandId = commandId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			else -> PersistOutcome.Indeterminate(
				commandId = commandId,
				error = s2error(
					code = "UNKNOWN_OUTCOME",
					description = outcome.outcome,
				),
			)
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
	val iteration: Int,
	val failure: PersistOutcome.Rejected<EVENT>? = null,
) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>
