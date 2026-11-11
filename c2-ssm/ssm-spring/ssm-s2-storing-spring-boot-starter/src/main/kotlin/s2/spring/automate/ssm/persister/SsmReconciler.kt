package s2.spring.automate.ssm.persister

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.InitTransitionAppliedContext
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2State
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

/**
 * Idempotent reconciliation: a transition that actually committed on a prior attempt
 * (lost response, sweeper retry, duplicate send, or a replay onto a non-wiped chain) comes
 * back as a [PersistOutcome.Failure]. Instead of guessing from the chaincode's error
 * prose, we ask the chain: if the session's log at the target iteration already holds the
 * exact state this command was writing, the transition is on chain — promote it to
 * [PersistOutcome.Success] and recover the real txId, instead of re-rejecting and flooding
 * the DLQ.
 *
 * Cost: zero reads on the success path; a single batched session-logs read only when at
 * least one outcome is a reconcilable failure.
 */
internal class SsmReconciler<EVENT>(
    private val objectMapper: ObjectMapper,
    private val sessionLogs: (Flow<GetAutomateSessionQuery>) -> Flow<SsmGetSessionLogsQueryResult>,
) {

    private val logger = LoggerFactory.getLogger(SsmAutomatePersister::class.java)

    // Any failure of the chain read must fall back to the base outcomes: reconciliation never throws.
    @Suppress("TooGenericExceptionCaught")
    suspend fun reconcile(candidates: List<ReconcileCandidate<EVENT>>): Map<String, PersistOutcome<EVENT>> {
        val toCheck = candidates.filter { it.base.isReconcilableFailure() }
        if (toCheck.isEmpty()) return emptyMap()

        val logsBySession = try {
            val queries = toCheck.map { candidate ->
                GetAutomateSessionQuery(automateContext = candidate.automateContext, sessionId = candidate.sessionName)
            }
            sessionLogs(queries.distinct().asFlow()).toList().associateBy { it.sessionName }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn("Reconciliation chain query failed; falling back to base outcomes", e)
            emptyMap()
        }

        return toCheck.associate { candidate ->
            candidate.msgId to reconcileOne(candidate = candidate, onChain = logsBySession[candidate.sessionName])
        }
    }

    private fun reconcileOne(
        candidate: ReconcileCandidate<EVENT>,
        onChain: SsmGetSessionLogsQueryResult?,
    ): PersistOutcome<EVENT> {
        val log = onChain?.run { logs.firstOrNull { it.state.iteration == candidate.targetIteration } }
        return if (log != null && isSameState(intended = candidate.intendedPublic, onChain = log.state.public)) {
            PersistOutcome.Success(
                msgId = candidate.msgId,
                event = candidate.event,
                metadata = mapOf("transactionId" to log.txId),
            )
        } else {
            candidate.base
        }
    }

    private fun isSameState(intended: String, onChain: Any?): Boolean {
        if (onChain == null) return false
        // Trees, not strings: serialisers may order keys differently. Unparseable falls back to raw equality.
        return runCatching {
            val intendedNode = objectMapper.readTree(intended)
            val onChainNode = if (onChain is String) {
                objectMapper.readTree(onChain)
            } else {
                objectMapper.valueToTree<JsonNode>(onChain)
            }
            intendedNode == onChainNode
        }.getOrDefault(intended == onChain.toString())
    }
}

/** A persisted command paired with the on-chain state it intended to write, for [SsmReconciler]. */
internal class ReconcileCandidate<EVENT>(
    val sessionName: SessionName,
    val targetIteration: Int,
    val intendedPublic: String,
    val event: EVENT,
    val automateContext: AutomateContext<S2Automate>,
    val base: PersistOutcome<EVENT>,
) {
    val msgId: String get() = base.msgId
}

/** Pairs an init context with the on-chain state its start command intends to write. */
internal fun <STATE, ID : Any, ENTITY, EVENT> reconcileCandidateOf(
    ctx: InitTransitionAppliedContext<STATE, ID, ENTITY, EVENT, S2Automate>,
    command: SsmStartCommand,
    base: PersistOutcome<EVENT>,
): ReconcileCandidate<EVENT> where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID> = ReconcileCandidate(
    sessionName = ctx.entity.s2Id().toString(),
    targetIteration = START_ITERATION,
    intendedPublic = command.session.public,
    event = ctx.event,
    automateContext = ctx.automateContext,
    base = base,
)

/** Pairs a transition's session result with the on-chain state its perform command intends to write. */
internal fun <STATE, ID : Any, ENTITY, EVENT> reconcileCandidateOf(
    result: GetSessionResult<STATE, ID, ENTITY, EVENT>,
    command: SsmPerformCommand,
    base: PersistOutcome<EVENT>,
): ReconcileCandidate<EVENT> where
STATE : S2State,
ENTITY : WithS2State<STATE>,
ENTITY : WithS2Id<ID> = ReconcileCandidate(
    sessionName = result.sessionId,
    targetIteration = result.iteration + 1,
    intendedPublic = command.context.public,
    event = result.transitionContext.event,
    automateContext = result.transitionContext.automateContext,
    base = base,
)

// Transient excluded: the tx may never have reached the chain, so it must retry, never reconcile.
private fun PersistOutcome<*>.isReconcilableFailure(): Boolean =
    this is PersistOutcome.Failure && this !is PersistOutcome.Transient

private const val START_ITERATION = 0
