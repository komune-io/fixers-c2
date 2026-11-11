package s2.spring.sourcing.ssm

import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import s2.dsl.automate.Evt
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2RoleValue
import s2.dsl.automate.S2StateValue
import s2.dsl.automate.S2Transition
import s2.dsl.automate.S2TransitionValue
import s2.dsl.automate.model.WithS2Id
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.SsmTransition
import ssm.chaincode.dsl.model.uri.ChaincodeUri

class EventPersisterSsmTest {

    @Serializable
    data class TestEvent(
        val eventId: String,
        val data: String
    ) : Evt, WithS2Id<String> {
        override fun s2Id() = eventId
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val format = SsmEventFormat(eventType = TestEvent::class, json = json)

    private fun ssmLog(iteration: Int, public: Any?): SsmSessionStateLog {
        return SsmSessionStateLog(
            txId = "tx-$iteration",
            state = SsmSessionState(
                ssm = "test-ssm",
                session = "session-1",
                roles = mapOf("admin" to "Admin"),
                public = public,
                private = emptyMap(),
                origin = SsmTransition(from = 0, to = 1, role = "Admin", action = "Create"),
                current = 1,
                iteration = iteration,
            )
        )
    }

    private fun sessions(isVersioning: Boolean) = SsmEventSessions<TestEvent, String>(
        s2Automate = automateWithTransitions(),
        functions = SsmSourcingFunctions(
            ssmSessionStartFunction = { error("not used in this test") },
            ssmSessionPerformActionFunction = { error("not used in this test") },
            dataSsmSessionGetQueryFunction = { error("not used in this test") },
            ssmGetSessionLogsQueryFunction = { error("not used in this test") },
            dataSsmSessionListQueryFunction = { error("not used in this test") },
        ),
        chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
        isVersioning = isVersioning,
    )

    @Test
    suspend fun `decode deserializes valid string public data`() {
        val event = TestEvent(eventId = "id-1", data = "hello")
        val encoded = json.encodeToString(TestEvent.serializer(), event)
        val logs = listOf(ssmLog(iteration = 0, public = encoded))

        val result = format.decode(logs).toList()

        assertThat(result).hasSize(1)
        assertThat(result[0].eventId).isEqualTo("id-1")
        assertThat(result[0].data).isEqualTo("hello")
    }

    @Test
    suspend fun `decode throws IllegalStateException when public is not a String`() {
        val logs = listOf(ssmLog(iteration = 0, public = 12_345))

        val exception = assertThrows<Exception> {
            format.decode(logs).toList()
        }
        val cause = exception.cause ?: exception
        assertThat(cause).isInstanceOf(IllegalStateException::class.java)
        assertThat(cause.message).contains("Expected state.public to be String")
    }

    @Test
    suspend fun `decode throws IllegalStateException when public is a map`() {
        val logs = listOf(ssmLog(iteration = 0, public = mapOf("key" to "value")))

        val exception = assertThrows<Exception> {
            format.decode(logs).toList()
        }
        val cause = exception.cause ?: exception
        assertThat(cause).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    suspend fun `decode sorts logs by iteration before deserializing`() {
        val event1 = TestEvent(eventId = "id-1", data = "first")
        val event2 = TestEvent(eventId = "id-1", data = "second")
        // Intentionally reversed order
        val logs = listOf(
            ssmLog(iteration = 1, public = json.encodeToString(TestEvent.serializer(), event2)),
            ssmLog(iteration = 0, public = json.encodeToString(TestEvent.serializer(), event1)),
        )

        val result = format.decode(logs).toList()

        assertThat(result).hasSize(2)
        assertThat(result[0].data).isEqualTo("first")
        assertThat(result[1].data).isEqualTo("second")
    }

    @Test
    suspend fun `decode handles empty log list`() {
        val result = format.decode(emptyList()).toList()
        assertThat(result).isEmpty()
    }

    @Test
    fun `nameOf without versioning returns plain id`() {
        val result = sessions(isVersioning = false).nameOf("event-123")
        assertThat(result).isEqualTo("event-123")
    }

    @Test
    fun `nameOf with versioning returns prefixed name`() {
        val result = sessions(isVersioning = true).nameOf("event-123")
        assertThat(result).isEqualTo("TestAutomate-event-123")
    }

    private fun automateWithTransitions(): S2Automate {
        return S2Automate(
            name = "TestAutomate",
            version = "1.0",
            transitions = arrayOf(
                S2Transition(
                    from = S2StateValue(name = "Init", position = 0),
                    to = S2StateValue(name = "Active", position = 1),
                    role = S2RoleValue(name = "Admin"),
                    action = S2TransitionValue(name = "Create"),
                    result = null,
                )
            )
        )
    }
}
