package s2.spring.automate.ssm.persister

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.persist.LoadOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2RoleValue
import s2.dsl.automate.S2State
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
import tools.jackson.module.kotlin.jacksonObjectMapper

/**
 * The storing layer skips the iteration pre-read (read #2) for entities implementing
 * [WithS2Iteration]; that only works if `loadWithOutcomes` stamps the on-chain iteration
 * onto the loaded entity. The iteration is chain metadata (`state.iteration`), not part of
 * the `public` payload — so it must be injected, overriding whatever (stale) value the
 * public JSON carries. This test pins that behaviour.
 */
class SsmAutomatePersisterLoadIterationTest {

    interface TestState : S2State { override val position: Int }

    data class IterableEntity(val id: String, val status: Int, val iteration: Int) :
        WithS2Id<String>, WithS2State<TestState>, WithS2Iteration {
        override fun s2Id() = id
        override fun s2State() = object : TestState { override val position = status }
        override fun s2Iteration() = iteration
        override fun withS2Iteration(iteration: Int) = copy(iteration = iteration)
    }

    data class TestEvt(val id: String = "") : Event

    /** Stamps its iteration into another type: the loader's cast to the entity type fails. */
    data class MisstampingEntity(val id: String, val status: Int) :
        WithS2Id<String>, WithS2State<TestState>, WithS2Iteration {
        override fun s2Id() = id
        override fun s2State() = object : TestState { override val position = status }
        override fun s2Iteration() = 0
        override fun withS2Iteration(iteration: Int): WithS2Iteration = IterableEntity(id, status, iteration)
    }

    private val om = jacksonObjectMapper()

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

    private fun logsAt(session: String, iteration: Int, publicJson: String) =
        SsmGetSessionLogsQueryResult(
            ssmName = "test-ssm",
            sessionName = session,
            logs = listOf(
                SsmSessionStateLog(
                    txId = "tx-$iteration",
                    state = SsmSessionState(
                        ssm = "test-ssm", session = session, roles = mapOf("a" to "A"),
                        public = publicJson, private = emptyMap(),
                        origin = SsmTransition(from = 0, to = 1, role = "A", action = "Act"),
                        current = 1, iteration = iteration,
                    )
                )
            ),
        )

    private fun persister(logs: SsmGetSessionLogsQueryFunction) = persister(logs, IterableEntity::class.java)

    private fun <ENTITY> persister(
        logs: SsmGetSessionLogsQueryFunction,
        entityType: Class<ENTITY>,
    ) where ENTITY : WithS2Id<String>, ENTITY : WithS2State<TestState> =
        SsmAutomatePersister<TestState, String, ENTITY, TestEvt>(
            functions = SsmSessionFunctions(
                ssmSessionStartFunction = F2Function { _ -> error("start not used") },
                ssmSessionPerformActionFunction = F2Function { _ -> error("perform not used") },
                ssmGetSessionLogsQueryFunction = logs,
            ),
            target = SsmSigningTarget(
                chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
                agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
            ),
            entityType = entityType,
            objectMapper = om,
            batch = S2BatchProperties(),
        )

    @Test
    suspend fun `loadWithOutcomes stamps the on-chain iteration onto the loaded entity`() {
        // public carries a STALE iteration (0); the chain head log is at iteration 7.
        val publicJson = """{"id":"s1","status":1,"iteration":0}"""
        val logs: SsmGetSessionLogsQueryFunction = F2Function { q ->
            q.toList().map { logsAt(it.sessionName, iteration = 7, publicJson) }.asFlow()
        }

        val out = persister(logs)
            .loadWithOutcomes(AutomateContext(automate = testAutomate, batch = S2BatchProperties()), flowOf("s1"))
            .toList()
            .single()

        val loaded = out as LoadOutcome.Loaded<String, IterableEntity>
        // injected from state.iteration, NOT the stale 0 in the public payload
        assertThat(loaded.entity.s2Iteration()).isEqualTo(7)
        assertThat(loaded.entity.id).isEqualTo("s1")
        assertThat(loaded.entity.status).isEqualTo(1)
    }

    @Test
    suspend fun `an entity that cannot be cast after its iteration is stamped is rejected for its id only`() {
        val logs: SsmGetSessionLogsQueryFunction = F2Function { q ->
            q.toList().map { logsAt(it.sessionName, iteration = 3, """{"id":"${it.sessionName}","status":1}""") }
                .asFlow()
        }

        val out = persister(logs, MisstampingEntity::class.java)
            .loadWithOutcomes(AutomateContext(automate = testAutomate, batch = S2BatchProperties()), flowOf("s1", "s2"))
            .toList()

        assertThat(out.map { it.id }).containsExactly("s1", "s2")
        out.forEach { outcome ->
            val rejected = outcome as LoadOutcome.Rejected<*, *>
            assertThat(rejected.error.type).isEqualTo("DESERIALIZATION_FAILED")
            assertThat(rejected.error.cause).isInstanceOf(ClassCastException::class.java)
        }
    }
}
