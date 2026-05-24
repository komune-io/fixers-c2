package s2.spring.automate.ssm.persister

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.InitTransitionAppliedContext
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2InitCommand
import s2.dsl.automate.S2RoleValue
import s2.dsl.automate.S2StateValue
import s2.dsl.automate.S2Transition
import s2.dsl.automate.S2TransitionValue
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.SsmTransition
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.sdk.dsl.CommandOutcome
import tools.jackson.databind.ObjectMapper

/**
 * Unit tests (no Spring) for SsmAutomatePersister.persistInitWithOutcomes — the START path.
 *
 * The existing SsmAutomatePersisterTest covers the PERFORM path (persistWithOutcomes).
 * This companion class pins the INIT (start) path.
 *
 * We reuse the same fixtures defined locally (parallel to SsmAutomatePersisterTest):
 * - TestState, IterableEntity, TestEvt, TestInitCommand
 * - testAutomate: minimal S2Automate with one transition
 * - makeInitTransitionContext: builds InitTransitionAppliedContext<..., IterableEntity, ...>
 *
 * Only the v2 start function is exercised; all other stubs throw if called.
 */
class SsmAutomatePersisterInitWithOutcomesTest {

    // ---------------------------------------------------------------------------
    // Shared fixtures (parallel to SsmAutomatePersisterTest)
    // ---------------------------------------------------------------------------

    interface TestState : s2.dsl.automate.S2State {
        override val position: Int
    }

    data class IterableEntity(
        val id: String,
        val status: Int,
        val iteration: Int,
    ) : WithS2Id<String>, WithS2State<TestState>, s2.dsl.automate.model.WithS2Iteration {
        override fun s2Id() = id
        override fun s2State() = object : TestState { override val position = status }
        override fun s2Iteration() = iteration
    }

    data class TestEvt(val id: String = "") : Event

    /** Minimal S2InitCommand implementation for test usage. */
    data class TestInitCommand(val entityId: String) : S2InitCommand

    private val testAutomate: S2Automate = S2Automate(
        name = "test-ssm",
        version = null,
        transitions = arrayOf(
            S2Transition(
                from = S2StateValue(name = "StateOne", position = 1),
                to = S2StateValue(name = "StateTwo", position = 2),
                role = S2RoleValue(name = "TestRole"),
                action = S2TransitionValue(name = "TestInitCommand"),
                result = null,
            )
        ),
    )

    private fun makeInitTransitionContext(
        entity: IterableEntity,
    ): InitTransitionAppliedContext<TestState, String, IterableEntity, TestEvt, S2Automate> =
        InitTransitionAppliedContext(
            automateContext = AutomateContext(automate = testAutomate, batch = S2BatchProperties()),
            msgId = "start:${entity.id}",
            msg = TestInitCommand(entityId = entity.id),
            event = TestEvt(id = entity.id),
            entity = entity,
        )

    /**
     * Build a SsmAutomatePersister<TestState, String, IterableEntity, TestEvt> with:
     * - A scripted v2 start function that returns [scriptedOutcomes] in order.
     * - All other functions (v1 start, v1 perform, v2 perform, session logs) throw if called.
     */
    private fun buildPersister(
        scriptedOutcomes: List<CommandOutcome>,
    ): SsmAutomatePersister<TestState, String, IterableEntity, TestEvt> {
        val v2Start: ssm.chaincode.f2.features.command.SsmTxSessionStartFunction =
            F2Function { commands ->
                val commandList = commands.toList()
                // Return outcomes indexed by position (not commandId) to keep the test simple.
                // The persister's own byId lookup uses commandId, so we must match commandIds.
                commandList.mapIndexed { i, cmd ->
                    scriptedOutcomes.getOrElse(i) {
                        CommandOutcome(
                            outcome = "Rejected",
                            msgId = cmd.msgId,
                            errorCode = "NO_SCRIPTED_OUTCOME",
                            errorMessage = "no scripted outcome for index $i",
                        )
                    }.copy(msgId = cmd.msgId)
                }.asFlow()
            }

        val noPerform: ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction =
            F2Function { _ -> error("perform should NOT be called by persistInitWithOutcomes") }

        val noLogs: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
            F2Function { _ -> error("ssmGetSessionLogsQueryFunction should NOT be called for IterableEntity") }

        return SsmAutomatePersister(
            ssmSessionStartFunction = v2Start,
            ssmSessionPerformActionFunction = noPerform,
            ssmGetSessionLogsQueryFunction = noLogs,
            chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
            entityType = IterableEntity::class.java,
            agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
            objectMapper = ObjectMapper(),
            batch = S2BatchProperties(),
        )
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `persistInitWithOutcomes maps Committed outcomes correctly`() = runTest {
        val persister = buildPersister(listOf(
            CommandOutcome(outcome = "Committed", msgId = "", transactionId = "tx-123", blockNumber = 42L),
        ))
        val ctx = makeInitTransitionContext(IterableEntity("entity-1", 1, 0))

        val outcomes = persister.persistInitWithOutcomes(flowOf(ctx)).toList()

        assertThat(outcomes).hasSize(1)
        val committed = outcomes.single() as PersistOutcome.Success<TestEvt>
        assertThat(committed.metadata["transactionId"]).isEqualTo("tx-123")
        assertThat(committed.metadata["blockNumber"]).isEqualTo("42")
        assertThat(committed.event.id).isEqualTo("entity-1")
    }

    @Test
    fun `persistInitWithOutcomes maps Rejected outcomes correctly`() = runTest {
        val persister = buildPersister(listOf(
            CommandOutcome(
                outcome = "Rejected",
                msgId = "",
                errorCode = "ENDORSE_FAILED",
                errorMessage = "endorsement failed",
            ),
        ))
        val ctx = makeInitTransitionContext(IterableEntity("entity-rej", 1, 0))

        val outcomes = persister.persistInitWithOutcomes(flowOf(ctx)).toList()

        assertThat(outcomes).hasSize(1)
        val rejected = outcomes.single() as PersistOutcome.Rejected<TestEvt>
        assertThat(rejected.error.type).isEqualTo("ENDORSE_FAILED")
        assertThat(rejected.error.description).isEqualTo("endorsement failed")
    }

    @Test
    fun `persistInitWithOutcomes maps Transient outcomes correctly`() = runTest {
        val persister = buildPersister(listOf(
            CommandOutcome(
                outcome = "Transient",
                msgId = "",
                errorCode = "GRPC_UNAVAILABLE",
                errorMessage = "peer down",
            ),
        ))
        val ctx = makeInitTransitionContext(IterableEntity("entity-tr", 1, 0))

        val outcomes = persister.persistInitWithOutcomes(flowOf(ctx)).toList()

        assertThat(outcomes).hasSize(1)
        val transient = outcomes.single() as PersistOutcome.Transient<TestEvt>
        assertThat(transient.error.type).isEqualTo("GRPC_UNAVAILABLE")
        assertThat(transient.error.description).isEqualTo("peer down")
    }

    @Test
    fun `persistInitWithOutcomes maps Indeterminate outcomes correctly`() = runTest {
        val persister = buildPersister(listOf(
            CommandOutcome(
                outcome = "Indeterminate",
                msgId = "",
                errorCode = "SUBMIT_FAILED",
                errorMessage = "orderer timeout",
            ),
        ))
        val ctx = makeInitTransitionContext(IterableEntity("entity-ind", 1, 0))

        val outcomes = persister.persistInitWithOutcomes(flowOf(ctx)).toList()

        assertThat(outcomes).hasSize(1)
        val indeterminate = outcomes.single() as PersistOutcome.Indeterminate<TestEvt>
        assertThat(indeterminate.error.type).isEqualTo("SUBMIT_FAILED")
        assertThat(indeterminate.error.description).isEqualTo("orderer timeout")
    }

    @Test
    fun `persistInitWithOutcomes maps Conflict outcomes correctly`() = runTest {
        val persister = buildPersister(listOf(
            CommandOutcome(
                outcome = "Conflict",
                msgId = "",
                errorCode = "MVCC_READ_CONFLICT",
                errorMessage = "conflict",
            ),
        ))
        val ctx = makeInitTransitionContext(IterableEntity("entity-cf", 1, 0))

        val outcomes = persister.persistInitWithOutcomes(flowOf(ctx)).toList()

        assertThat(outcomes).hasSize(1)
        val conflict = outcomes.single() as PersistOutcome.Conflict<TestEvt>
        assertThat(conflict.error.type).isEqualTo("MVCC_READ_CONFLICT")
        assertThat(conflict.error.description).isEqualTo("conflict")
    }

    @Test
    fun `persistInitWithOutcomes preserves commandId per input`() = runTest {
        val persister = buildPersister(listOf(
            CommandOutcome(outcome = "Committed", msgId = "", transactionId = "tx-A", blockNumber = 1L),
            CommandOutcome(outcome = "Rejected",  msgId = "", errorCode = "ERR", errorMessage = "err"),
        ))
        val ctx1 = makeInitTransitionContext(IterableEntity("entity-A", 1, 0))
        val ctx2 = makeInitTransitionContext(IterableEntity("entity-B", 1, 0))

        val outcomes = persister.persistInitWithOutcomes(flowOf(ctx1, ctx2)).toList()

        assertThat(outcomes).hasSize(2)
        // msgId is "start:<entityId>" (set by SsmAutomatePersister.persistInitWithOutcomes)
        assertThat(outcomes[0].msgId).isEqualTo("start:entity-A")
        assertThat(outcomes[1].msgId).isEqualTo("start:entity-B")
    }

    @Test
    fun `persistInitWithOutcomes emits one outcome per input`() = runTest {
        val scripted = listOf(
            CommandOutcome(outcome = "Committed",     msgId = "", transactionId = "tx-1", blockNumber = 1L),
            CommandOutcome(outcome = "Rejected",      msgId = "", errorCode = "E", errorMessage = "m"),
            CommandOutcome(outcome = "Transient",     msgId = "", errorCode = "T", errorMessage = "m"),
            CommandOutcome(outcome = "Indeterminate", msgId = "", errorCode = "I", errorMessage = "m"),
            CommandOutcome(outcome = "Conflict",      msgId = "", errorCode = "C", errorMessage = "m"),
        )
        val persister = buildPersister(scripted)
        val contexts = scripted.mapIndexed { i, _ ->
            makeInitTransitionContext(IterableEntity("entity-$i", 1, 0))
        }

        val outcomes = persister.persistInitWithOutcomes(contexts.asFlow()).toList()

        assertThat(outcomes).hasSize(5)
        assertThat(outcomes.filterIsInstance<PersistOutcome.Success<TestEvt>>()).hasSize(1)
        assertThat(outcomes.filterIsInstance<PersistOutcome.Rejected<TestEvt>>()).hasSize(1)
        assertThat(outcomes.filterIsInstance<PersistOutcome.Transient<TestEvt>>()).hasSize(1)
        assertThat(outcomes.filterIsInstance<PersistOutcome.Indeterminate<TestEvt>>()).hasSize(1)
        assertThat(outcomes.filterIsInstance<PersistOutcome.Conflict<TestEvt>>()).hasSize(1)
    }
}
