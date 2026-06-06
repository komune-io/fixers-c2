package s2.spring.automate.ssm.persister

import f2.dsl.fnc.operators.batchFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.InitTransitionAppliedContext
import s2.automate.core.context.TransitionAppliedContext
import s2.automate.core.context.asBatch
import s2.automate.core.persist.AutomatePersister
import s2.automate.core.persist.LoadOutcome
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
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.dsl.CommandOutcome
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
	internal var permissive: Boolean = false
) : AutomatePersister<STATE, ID, ENTITY, EVENT, S2Automate> where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID> {

	override suspend fun load(automateContexts: AutomateContext<S2Automate>, id: ID & Any): ENTITY? {
		return load(automateContexts, flowOf(id)).firstOrNull()
	}

	/**
	 * Per-id classified load. The engine's `evolveWithOutcomes` path consumes
	 * this method directly — overriding it (vs the legacy [load]) is what lets
	 * a single bad session in a batch surface as one [LoadOutcome.Rejected]
	 * instead of aborting the whole chunk.
	 *
	 * Failure modes:
	 *  - chaincode-query throws (network / gRPC) → [LoadOutcome.Transient] for
	 *    every id (we can't classify per-id when the query itself failed);
	 *  - session not in the result set (chain has no such id) →
	 *    [LoadOutcome.Rejected] with `SESSION_NOT_FOUND`;
	 *  - session returned but logs empty → [LoadOutcome.Rejected] with
	 *    `SESSION_NOT_INITIALIZED` (the originally-broken case);
	 *  - JSON deserialisation of the last log fails → [LoadOutcome.Rejected]
	 *    with `DESERIALIZATION_FAILED` (a bad on-chain write — retry won't fix).
	 */
	override suspend fun loadWithOutcomes(
		automateContexts: AutomateContext<S2Automate>,
		ids: Flow<ID & Any>,
	): Flow<LoadOutcome<ID & Any, ENTITY>> = flow {
		val idList = ids.toList()
		if (idList.isEmpty()) return@flow

		// distinct() — multiple commands in a batch may target the same entity;
		// the chaincode lookup is the same gRPC call, so dedup the queries.
		// Outcomes are still emitted per-original-id in the loop below.
		val queries = idList.distinct().map { id ->
			GetAutomateSessionQuery(automateContext = automateContexts, sessionId = id.toString())
		}

		val sessions = try {
			getSessionForAutomate(queries.asFlow()).toList()
		} catch (e: CancellationException) {
			// Cooperative cancellation must propagate — never swallow it into
			// a Transient outcome.
			throw e
		} catch (e: Exception) {
			// Whole chain query failed — we can't classify per id, so every id
			// becomes Transient (network/timeout-shaped — retry with backoff).
			idList.forEach { id ->
				emit(LoadOutcome.Transient<ID & Any, ENTITY>(id,
					s2error(
						code = "CHAINCODE_QUERY_FAILED",
						description = e.message ?: e::class.simpleName ?: "<no message>",
						payload = mapOf("id" to id.toString()),
						cause = e,
					)))
			}
			return@flow
		}

		val sessionsByName = sessions.associateBy { it.sessionName }
		idList.forEach { id ->
			val sessionName = id.toString()
			val session = sessionsByName[sessionName]
			when {
				session == null -> emit(LoadOutcome.Rejected<ID & Any, ENTITY>(id,
					s2error(
						code = "SESSION_NOT_FOUND",
						description = "Session $sessionName not on chaincode",
						payload = mapOf("id" to sessionName),
					)))
				session.logs.isEmpty() -> emit(LoadOutcome.Rejected<ID & Any, ENTITY>(id,
					s2error(
						code = "SESSION_NOT_INITIALIZED",
						description = "No logs for session $sessionName",
						payload = mapOf("id" to sessionName),
					)))
				else -> {
					val lastTransaction = session.logs.maxBy { it.state.iteration }
					val entity = try {
						objectMapper.readValue(lastTransaction.state.public.toString(), entityType)
					} catch (e: CancellationException) {
						throw e
					} catch (e: Exception) {
						// Bad write on-chain — Rejected (permanent: retry won't make
						// the corrupt payload parse).
						emit(LoadOutcome.Rejected<ID & Any, ENTITY>(id,
							s2error(
								code = "DESERIALIZATION_FAILED",
								description = e.message ?: e::class.simpleName ?: "<no message>",
								payload = mapOf("id" to sessionName),
								cause = e,
							)))
						return@forEach
					}
					emit(LoadOutcome.Loaded(id, entity))
				}
			}
		}
	}

	/**
	 * Legacy load — delegates to [loadWithOutcomes] and collapses each outcome
	 * to the nullable shape required by the interface signature:
	 *  - Loaded   → entity
	 *  - Rejected → null   (permanent: no entity, won't change on retry)
	 *  - any other Failure (Transient / Indeterminate / Conflict) → throw,
	 *    preserving legacy semantics for callers that don't go through the
	 *    WithOutcomes path (the original cause is rethrown when present so
	 *    the caller can introspect it).
	 *
	 * Uses the same `is Success / is Failure` idiom as [persistInit] /
	 * [persist] below — the explicit [LoadOutcome.Rejected] arm pre-empts
	 * [LoadOutcome.Failure] for the "return null" case; everything else falls
	 * into the catch-all Failure arm.
	 */
	override suspend fun load(automateContexts: AutomateContext<S2Automate>, ids: Flow<ID & Any>): Flow<ENTITY?> =
		loadWithOutcomes(automateContexts, ids).map { outcome ->
			when (outcome) {
				is LoadOutcome.Loaded -> outcome.entity
				is LoadOutcome.Rejected -> null
				is LoadOutcome.Failure -> throw outcome.error.cause
					?: IllegalStateException(outcome.error.description)
			}
		}

	override suspend fun persistInit(
		transitionContexts: Flow<InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
	): Flow<EVENT> = persistInitWithOutcomes(transitionContexts).mapNotNull { outcome ->
		when (outcome) {
			is PersistOutcome.Success -> outcome.event
			is PersistOutcome.Failure -> null
		}
	}

	override suspend fun persistInitWithOutcomes(
		transitionContexts: Flow<InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
	): Flow<PersistOutcome<EVENT>> = flow {
		val collectedContexts = transitionContexts.toList()

		val commands = collectedContexts.map { ctx ->
			val entity = ctx.entity
			val automate = ctx.automateContext.automate
			SsmStartCommand(
				msgId = "start:${entity.s2Id()}",
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

		val outcomes = ssmSessionStartFunction.invoke(commands.asFlow()).toList()
		val byId = outcomes.associateBy { it.msgId }

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
								msgId = commandIdFor(context),
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
							msgId = commandIdFor(miss),
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
	): Flow<EVENT> = persistWithOutcomes(transitionContexts).mapNotNull { outcome ->
		when (outcome) {
			is PersistOutcome.Success -> outcome.event
			is PersistOutcome.Failure -> null
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
		val commands = good.map { sr ->
			val entity = sr.transitionContext.entity
			val withEventAsAction = sr.transitionContext.automateContext.automate.withResultAsAction
			val action = sr.transitionContext.event?.takeIf { withEventAsAction } ?: sr.transitionContext.msg
			SsmPerformCommand(
				msgId = "${sr.sessionId}:${sr.iteration + 1}",
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

		val outcomes = ssmSessionPerformActionFunction.invoke(commands.asFlow()).toList()
		val byId = outcomes.associateBy { it.msgId }

		good.forEach { sr ->
			val cid = "${sr.sessionId}:${sr.iteration + 1}"
			@Suppress("UNCHECKED_CAST")
			emit(toPersistOutcome(cid, sr.transitionContext.event as EVENT, byId[cid]))
		}
	}

	private fun <E> toPersistOutcome(msgId: String, event: E, outcome: CommandOutcome?): PersistOutcome<E> {
		if (outcome == null) {
			return PersistOutcome.Indeterminate(
				msgId = msgId,
				error = s2error(
					code = "MISSING_OUTCOME",
					description = "No CommandOutcome returned for $msgId",
				),
			)
		}
		return when (outcome.outcome) {
			"Committed" -> PersistOutcome.Success(
				msgId = msgId,
				event = event,
				metadata = buildMap {
					outcome.transactionId?.let { put("transactionId", it) }
					outcome.blockNumber?.let { put("blockNumber", it.toString()) }
				},
			)
			"Rejected" -> PersistOutcome.Rejected(
				msgId = msgId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			"Transient" -> PersistOutcome.Transient(
				msgId = msgId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			"Indeterminate" -> PersistOutcome.Indeterminate(
				msgId = msgId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			"Conflict" -> PersistOutcome.Conflict(
				msgId = msgId,
				error = s2error(
					code = outcome.errorCode.orEmpty(),
					description = outcome.errorMessage.orEmpty(),
				),
			)
			else -> PersistOutcome.Indeterminate(
				msgId = msgId,
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
