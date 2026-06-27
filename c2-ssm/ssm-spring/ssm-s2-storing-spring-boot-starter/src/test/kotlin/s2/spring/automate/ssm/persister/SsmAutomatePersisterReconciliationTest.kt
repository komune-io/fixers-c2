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
import s2.automate.core.context.TransitionAppliedContext
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2Command
import s2.dsl.automate.S2InitCommand
import s2.dsl.automate.S2RoleValue
import s2.dsl.automate.S2StateValue
import s2.dsl.automate.S2Transition
import s2.dsl.automate.S2TransitionValue
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2Iteration
import s2.dsl.automate.model.WithS2State
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.SsmTransition
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.sdk.dsl.CommandOutcome
import tools.jackson.databind.ObjectMapper

/**
 * Idempotent reconciliation: a transition that already committed on a prior attempt comes back
 * as a Failure; the persister queries the chain and, if the on-chain state at the target
 * iteration matches what this command intended to write, promotes it to Success with the real
 * txId — instead of re-rejecting (which would flood the DLQ). No prose matching.
 *
 * Uses [IterableEntity] (WithS2Iteration) so `getIterations` skips the iteration pre-read — the
 * session-logs query is therefore invoked ONLY by reconciliation, making the assertions exact.
 */
class SsmAutomatePersisterReconciliationTest {

    interface TestState : s2.dsl.automate.S2State { override val position: Int }

    data class IterableEntity(val id: String, val status: Int, val iteration: Int) :
        WithS2Id<String>, WithS2State<TestState>, WithS2Iteration {
        override fun s2Id() = id
        override fun s2State() = object : TestState { override val position = status }
        override fun s2Iteration() = iteration
        override fun withS2Iteration(iteration: Int) = copy(iteration = iteration)
    }

    data class TestEvt(val id: String = "") : Event
    data class TestInitCommand(val entityId: String) : S2InitCommand
    data class TestCommand(override val id: String) : S2Command<String>

    private val om = ObjectMapper()

    private val testAutomate = S2Automate(
        name = "test-ssm",
        version = null,
        transitions = arrayOf(
            S2Transition(
                from = S2StateValue(name = "StateOne", position = 1),
                to = S2StateValue(name = "StateTwo", position = 2),
                role = S2RoleValue(name = "TestRole"),
                action = S2TransitionValue(name = "TestCommand"),
                result = null,
            )
        ),
    )

    private fun initCtx(entity: IterableEntity) =
        InitTransitionAppliedContext<TestState, String, IterableEntity, TestEvt, S2Automate>(
            automateContext = AutomateContext(automate = testAutomate, batch = S2BatchProperties()),
            msgId = "start:${entity.id}",
            msg = TestInitCommand(entityId = entity.id),
            event = TestEvt(id = entity.id),
            entity = entity,
        )

    private fun performCtx(
        entity: IterableEntity,
    ): TransitionAppliedContext<TestState, String, IterableEntity, TestEvt, S2Automate> {
        val from: TestState = object : TestState { override val position = entity.status }
        return TransitionAppliedContext(
            automateContext = AutomateContext(automate = testAutomate, batch = S2BatchProperties()),
            msgId = entity.id,
            from = from,
            msg = TestCommand(id = entity.id),
            event = TestEvt(id = entity.id),
            entity = entity,
        )
    }

    /** A logs result for [session] with one log at [iteration] carrying [publicJson]. */
    private fun logsAt(session: String, iteration: Int, publicJson: String, txId: String) =
        SsmGetSessionLogsQueryResult(
            ssmName = "test-ssm",
            sessionName = session,
            logs = listOf(
                SsmSessionStateLog(
                    txId = txId,
                    state = SsmSessionState(
                        ssm = "test-ssm", session = session, roles = mapOf("a" to "A"),
                        public = publicJson, private = emptyMap(),
                        origin = SsmTransition(from = 0, to = 1, role = "A", action = "Act"),
                        current = 1, iteration = iteration,
                    )
                )
            ),
        )

    private fun persister(
        start: SsmTxSessionStartFunction,
        perform: SsmTxSessionPerformActionFunction,
        logs: SsmGetSessionLogsQueryFunction,
    ) = SsmAutomatePersister<TestState, String, IterableEntity, TestEvt>(
        ssmSessionStartFunction = start,
        ssmSessionPerformActionFunction = perform,
        ssmGetSessionLogsQueryFunction = logs,
        chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
        entityType = IterableEntity::class.java,
        agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
        objectMapper = om,
        batch = S2BatchProperties(),
    )

    private fun rejected(msgId: String) = CommandOutcome(
        outcome = "Rejected", msgId = msgId, errorCode = "ENDORSE_FAILED", errorMessage = "session already exists",
    )

    private fun conflict(msgId: String) = CommandOutcome(
        outcome = "Conflict", msgId = msgId, errorCode = "MVCC_READ_CONFLICT", errorMessage = "stale read",
    )

    private fun committed(msgId: String, txId: String) =
        CommandOutcome(outcome = "Committed", msgId = msgId, transactionId = txId, blockNumber = 1L)

    private fun transient(msgId: String) = CommandOutcome(
        outcome = "Transient", msgId = msgId, errorCode = "GRPC_UNAVAILABLE", errorMessage = "peer down",
    )

    private val logsQueryMustNotBeCalled: SsmGetSessionLogsQueryFunction =
        F2Function { _ -> error("logs query must not be called when nothing is reconcilable") }

    // ── START ───────────────────────────────────────────────────────────────────

    @Test
    fun `START Rejected is reconciled to Success when the session already exists with matching state`() = runTest {
        val entity = IterableEntity("id-1", status = 1, iteration = 0)
        val onChainPublic = om.writeValueAsString(entity)
        val logs: SsmGetSessionLogsQueryFunction = F2Function { q ->
            q.toList()
                .map { logsAt(it.sessionName, iteration = 0, onChainPublic, txId = "tx-start-recovered") }
                .asFlow()
        }
        val p = persister(
            start = F2Function { c -> c.toList().map { rejected(it.msgId) }.asFlow() },
            perform = F2Function { _ -> error("perform not used") },
            logs = logs,
        )

        val out = p.persistInitWithOutcomes(flowOf(initCtx(entity))).toList().single()

        val success = out as PersistOutcome.Success<TestEvt>
        assertThat(success.metadata["transactionId"]).isEqualTo("tx-start-recovered")
        assertThat(success.event.id).isEqualTo("id-1")
    }

    @Test
    fun `START Rejected stays Rejected when the on-chain state does not match`() = runTest {
        val entity = IterableEntity("id-2", status = 1, iteration = 0)
        val logs: SsmGetSessionLogsQueryFunction = F2Function { q ->
            q.toList()
                .map { logsAt(it.sessionName, iteration = 0, publicJson = "{\"other\":true}", txId = "tx-x") }
                .asFlow()
        }
        val p = persister(
            start = F2Function { c -> c.toList().map { rejected(it.msgId) }.asFlow() },
            perform = F2Function { _ -> error("perform not used") },
            logs = logs,
        )

        val out = p.persistInitWithOutcomes(flowOf(initCtx(entity))).toList().single()

        assertThat(out).isInstanceOf(PersistOutcome.Rejected::class.java)
        assertThat((out as PersistOutcome.Rejected).error.type).isEqualTo("ENDORSE_FAILED")
    }

    @Test
    fun `START Committed never triggers a reconciliation query`() = runTest {
        val entity = IterableEntity("id-ok", status = 1, iteration = 0)
        val p = persister(
            start = F2Function { c -> c.toList().map { committed(it.msgId, "tx-committed") }.asFlow() },
            perform = F2Function { _ -> error("perform not used") },
            logs = logsQueryMustNotBeCalled,
        )

        val out = p.persistInitWithOutcomes(flowOf(initCtx(entity))).toList().single()

        assertThat((out as PersistOutcome.Success<TestEvt>).metadata["transactionId"]).isEqualTo("tx-committed")
    }

    // ── PERFORM ─────────────────────────────────────────────────────────────────

    @Test
    fun `PERFORM Conflict is reconciled to Success when the target iteration is already on chain`() = runTest {
        val entity = IterableEntity("id-3", status = 1, iteration = 0)
        val onChainPublic = om.writeValueAsString(entity)
        val logs: SsmGetSessionLogsQueryFunction = F2Function { q ->
            q.toList()
                .map { logsAt(it.sessionName, iteration = 1, onChainPublic, txId = "tx-perform-recovered") }
                .asFlow()
        }
        val p = persister(
            start = F2Function { _ -> error("start not used") },
            perform = F2Function { c -> c.toList().map { conflict(it.msgId) }.asFlow() },
            logs = logs,
        )

        val out = p.persistWithOutcomes(flowOf(performCtx(entity))).toList().single()

        val success = out as PersistOutcome.Success<TestEvt>
        assertThat(success.metadata["transactionId"]).isEqualTo("tx-perform-recovered")
    }

    @Test
    fun `PERFORM Transient is never reconciled and never queries the chain`() = runTest {
        val entity = IterableEntity("id-4", status = 1, iteration = 0)
        val p = persister(
            start = F2Function { _ -> error("start not used") },
            perform = F2Function { c -> c.toList().map { transient(it.msgId) }.asFlow() },
            logs = logsQueryMustNotBeCalled,
        )

        val out = p.persistWithOutcomes(flowOf(performCtx(entity))).toList().single()

        assertThat((out as PersistOutcome.Transient).error.type).isEqualTo("GRPC_UNAVAILABLE")
    }
}
