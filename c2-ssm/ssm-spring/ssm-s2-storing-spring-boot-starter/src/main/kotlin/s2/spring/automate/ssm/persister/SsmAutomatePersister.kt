package s2.spring.automate.ssm.persister

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.InitTransitionAppliedContext
import s2.automate.core.context.TransitionAppliedContext
import s2.automate.core.error.asException
import s2.automate.core.persist.AutomatePersister
import s2.automate.core.persist.LoadOutcome
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.ErrorCategory
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2ErrorBase
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.dsl.automate.s2error
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.dsl.CommandOutcome
import tools.jackson.databind.ObjectMapper

class SsmAutomatePersister<STATE, ID : Any, ENTITY, EVENT>(
    private val functions: SsmSessionFunctions,
    private val target: SsmSigningTarget,
    entityType: Class<ENTITY>,
    objectMapper: ObjectMapper,
    batch: S2BatchProperties,
    /**
     * Resolves an optional business timestamp (epoch millis) from the S2 command driving a transition
     * (`InitTransitionAppliedContext.msg` / `TransitionAppliedContext.msg`). When it returns a value, the
     * Fabric tx envelope is stamped with that time instead of wall-clock. Defaults to null (wall-clock).
     */
    timestampProvider: (Any) -> Long? = { null },
) : AutomatePersister<STATE, ID, ENTITY, EVENT, S2Automate> where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID> {

    private val entityLoader = SsmEntityLoader<ID, ENTITY>(
        objectMapper = objectMapper,
        entityType = entityType,
        sessionLogs = ::sessionLogs,
    )
    private val iterations = SsmSessionIterations<STATE, ID, ENTITY, EVENT>(
        entityType = entityType,
        batch = batch,
        sessionLogs = ::sessionLogs,
    )
    private val commands = SsmCommandFactory<STATE, ID, ENTITY, EVENT>(
        target = target,
        objectMapper = objectMapper,
        timestampProvider = timestampProvider,
    )
    private val reconciler = SsmReconciler<EVENT>(objectMapper = objectMapper, sessionLogs = ::sessionLogs)

    override suspend fun load(automateContexts: AutomateContext<S2Automate>, id: ID): ENTITY? =
        load(automateContexts, flowOf(id)).firstOrNull()

    /**
     * Per-id classified load, see [SsmEntityLoader]. The engine's `evolveWithOutcomes` path consumes
     * this method directly — overriding it (vs the legacy [load]) is what lets a single bad session in
     * a batch surface as one [LoadOutcome.Rejected] instead of aborting the whole chunk.
     */
    override fun loadWithOutcomes(
        automateContexts: AutomateContext<S2Automate>,
        ids: Flow<ID>,
    ): Flow<LoadOutcome<ID, ENTITY>> = flow {
        emitAll(entityLoader.load(automateContext = automateContexts, ids = ids.toList()).asFlow())
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
    override fun load(automateContexts: AutomateContext<S2Automate>, ids: Flow<ID>): Flow<ENTITY?> =
        loadWithOutcomes(automateContexts, ids).map { outcome ->
            when (outcome) {
                is LoadOutcome.Loaded -> outcome.entity
                is LoadOutcome.Rejected -> null
                is LoadOutcome.Failure -> throw outcome.error.cause ?: error(outcome.error.description)
            }
        }

    /**
     * Legacy init persist — collapses [persistInitWithOutcomes] for callers that don't go
     * through the outcomes path. A [PersistOutcome.Failure] must throw rather than be
     * dropped: dropping it under-emits and desynchronises the caller's command/event pairing.
     */
    override fun persistInit(
        transitionContexts: Flow<InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
    ): Flow<EVENT> = persistInitWithOutcomes(transitionContexts).map { outcome ->
        when (outcome) {
            is PersistOutcome.Success -> outcome.event
            is PersistOutcome.Failure -> throw outcome.asCategorizedException()
        }
    }

    override fun persistInitWithOutcomes(
        transitionContexts: Flow<InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
    ): Flow<PersistOutcome<EVENT>> = flow {
        emitReconciled(
            items = transitionContexts.toList(),
            pipeline = PersistPipeline(
                buildCommand = commands::start,
                msgIdOf = SsmStartCommand::msgId,
                send = { startCommands -> functions.ssmSessionStartFunction.invoke(startCommands) },
                eventOf = { ctx -> ctx.event },
                toCandidate = { ctx, command, base -> reconcileCandidateOf(ctx = ctx, command = command, base = base) },
            ),
        )
    }

    /**
     * Legacy persist — collapses [persistWithOutcomes] for the legacy engine path.
     *
     * The [AutomatePersister.persist] contract requires exactly one event per received
     * context, in the same order: the legacy engine correlates events to contexts
     * positionally ([s2.automate.core.engine.S2AutomateEngineImpl.doTransition] guards
     * this with ERROR_PERSISTER_EVENT_COUNT). A [PersistOutcome.Failure] must therefore
     * throw rather than be dropped — a dropped event shifts every later event one
     * position left and misroutes the engine's end-of-transition application events.
     */
    override fun persist(
        transitionContexts: Flow<TransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
    ): Flow<EVENT> = persistWithOutcomes(transitionContexts).map { outcome ->
        when (outcome) {
            is PersistOutcome.Success -> outcome.event
            is PersistOutcome.Failure -> throw outcome.asCategorizedException()
        }
    }

    override fun persistWithOutcomes(
        transitionContexts: Flow<TransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>>
    ): Flow<PersistOutcome<EVENT>> = flow {
        val queries = transitionContexts.toList().map { ctx ->
            GetSessionQuery(transitionContext = ctx, sessionId = ctx.entity.s2Id().toString())
        }
        val sessionResults = iterations.iterationsOf(queries.asFlow()).toList()

        sessionResults.mapNotNull { it.failure }.forEach { failure -> emit(failure) }

        emitReconciled(
            items = sessionResults.filter { it.failure == null },
            pipeline = PersistPipeline(
                buildCommand = commands::perform,
                msgIdOf = SsmPerformCommand::msgId,
                send = { performCommands -> functions.ssmSessionPerformActionFunction.invoke(performCommands) },
                eventOf = { result -> result.transitionContext.event },
                toCandidate = { result, command, base ->
                    reconcileCandidateOf(result = result, command = command, base = base)
                },
            ),
        )
    }

    // Outcomes are matched back to items by msgId, whose scheme each pipeline's buildCommand owns.
    private suspend fun <ITEM, COMMAND> FlowCollector<PersistOutcome<EVENT>>.emitReconciled(
        items: List<ITEM>,
        pipeline: PersistPipeline<ITEM, COMMAND, EVENT>,
    ) {
        val commandList = items.map(pipeline.buildCommand)
        val outcomesById = pipeline.send(commandList.asFlow()).toList().associateBy { it.msgId }
        val candidates = items.zip(commandList).map { (item, command) ->
            val msgId = pipeline.msgIdOf(command)
            val base = persistOutcomeOf(outcome = outcomesById[msgId], msgId = msgId, event = pipeline.eventOf(item))
            pipeline.toCandidate(item, command, base)
        }
        val reconciled = reconciler.reconcile(candidates)
        candidates.forEach { emit(reconciled[it.msgId] ?: it.base) }
    }

    private fun sessionLogs(queries: Flow<GetAutomateSessionQuery>): Flow<SsmGetSessionLogsQueryResult> = flow {
        val logsQueries = queries.map { query ->
            SsmGetSessionLogsQuery(
                sessionName = query.sessionId,
                chaincodeUri = target.chaincodeUri,
                ssmName = query.automateContext.automate.name,
            )
        }
        emitAll(functions.ssmGetSessionLogsQueryFunction.invoke(logsQueries))
    }
}

/** How one kind of transition is turned into chaincode commands, outcomes and reconcile candidates. */
private data class PersistPipeline<ITEM, COMMAND, EVENT>(
    val buildCommand: (ITEM) -> COMMAND,
    val msgIdOf: (COMMAND) -> String,
    val send: suspend (Flow<COMMAND>) -> Flow<CommandOutcome>,
    val eventOf: (ITEM) -> EVENT,
    val toCandidate: (ITEM, COMMAND, PersistOutcome<EVENT>) -> ReconcileCandidate<EVENT>,
)

private fun <E> persistOutcomeOf(outcome: CommandOutcome?, msgId: String, event: E): PersistOutcome<E> {
    if (outcome == null) {
        return PersistOutcome.Indeterminate(
            msgId = msgId,
            error = s2error(
                code = "MISSING_OUTCOME",
                description = "No CommandOutcome returned for $msgId",
            ),
        )
    }
    val error = s2error(
        code = outcome.errorCode.orEmpty(),
        description = outcome.errorMessage.orEmpty(),
    )
    return when (outcome.outcome) {
        "Committed" -> PersistOutcome.Success(
            msgId = msgId,
            event = event,
            metadata = buildMap {
                outcome.transactionId?.let { this["transactionId"] = it }
                outcome.blockNumber?.let { this["blockNumber"] = it.toString() }
            },
        )
        "Rejected" -> PersistOutcome.Rejected(msgId = msgId, error = error)
        "Transient" -> PersistOutcome.Transient(msgId = msgId, error = error)
        "Indeterminate" -> PersistOutcome.Indeterminate(msgId = msgId, error = error)
        "Conflict" -> PersistOutcome.Conflict(msgId = msgId, error = error)
        else -> PersistOutcome.Indeterminate(
            msgId = msgId,
            error = s2error(
                code = "UNKNOWN_OUTCOME",
                description = outcome.outcome,
            ),
        )
    }
}

/** Payload key under which the legacy persist paths carry [PersistOutcome.Failure.category]. */
internal const val CATEGORY_PAYLOAD_KEY = "category"

/**
 * Collapses a [PersistOutcome.Failure] into the [s2.automate.core.error.AutomateException] that the
 * legacy [AutomatePersister.persist] / [AutomatePersister.persistInit] contract throws, carrying the
 * outcome's [ErrorCategory] into the error payload under [CATEGORY_PAYLOAD_KEY].
 *
 * Throwing `error.asException()` bare would drop the category, collapsing all four failure variants
 * into one opaque exception. On a ledger that distinction is load-bearing: [ErrorCategory.Transient]
 * is safe to retry, whereas [ErrorCategory.Indeterminate] means the command may already have
 * committed, so a blind retry risks a duplicate on-chain transition — and [ErrorCategory.Rejected] is
 * permanent, so retrying is pointless. Legacy callers only see the exception, never the outcome, so
 * the payload is the sole remaining channel for that signal.
 *
 * Built via [S2ErrorBase] rather than the `s2error` factory because the factory hardcodes `date = ""`;
 * everything but the added payload entry is copied verbatim.
 *
 * Internal rather than private so the module's tests can pin the merge contract directly — the
 * persister never builds a failure with a non-empty payload, so an end-to-end test cannot prove
 * that pre-existing payload entries survive.
 */
internal fun PersistOutcome.Failure<*>.asCategorizedException() = S2ErrorBase(
    type = error.type,
    description = error.description,
    date = error.date,
    payload = error.payload + (CATEGORY_PAYLOAD_KEY to category.name),
    cause = error.cause,
).asException()

data class GetSessionQuery<STATE, ID : Any, ENTITY, EVENT>(
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

data class GetSessionResult<STATE, ID : Any, ENTITY, EVENT>(
    val transitionContext: TransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>,
    val sessionId: SessionName,
    val iteration: Int,
    val failure: PersistOutcome.Rejected<EVENT>? = null,
) where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID>
