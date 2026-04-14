package s2.spring.sourcing.ssm

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
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

class EventPersisterSsmTest {

	@Serializable
	data class TestEvent(
		val eventId: String,
		val data: String
	) : Evt, WithS2Id<String> {
		override fun s2Id() = eventId
	}

	private val json = Json { ignoreUnknownKeys = true }

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

	private fun createPersister(): EventPersisterSsm<TestEvent, String> {
		return EventPersisterSsm(
			s2Automate = automateWithTransitions(),
			eventType = TestEvent::class,
			batchParams = s2.automate.core.config.S2BatchProperties(),
		).also {
			it.json = json
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun invokeToEvents(
		persister: EventPersisterSsm<TestEvent, String>,
		logs: List<SsmSessionStateLog>
	): kotlinx.coroutines.flow.Flow<TestEvent> {
		val method = EventPersisterSsm::class.java.getDeclaredMethod("toEvents", List::class.java)
		method.isAccessible = true
		return method.invoke(persister, logs) as kotlinx.coroutines.flow.Flow<TestEvent>
	}

	@Test
	fun `toEvents deserializes valid string public data`() = runTest {
		val event = TestEvent("id-1", "hello")
		val encoded = json.encodeToString(TestEvent.serializer(), event)
		val logs = listOf(ssmLog(0, encoded))

		val result = invokeToEvents(createPersister(), logs).toList()

		assertThat(result).hasSize(1)
		assertThat(result[0].eventId).isEqualTo("id-1")
		assertThat(result[0].data).isEqualTo("hello")
	}

	@Test
	fun `toEvents throws IllegalStateException when public is not a String`() = runTest {
		val logs = listOf(ssmLog(0, 12345))

		val exception = assertThrows<Exception> {
			invokeToEvents(createPersister(), logs).toList()
		}
		val cause = exception.cause ?: exception
		assertThat(cause).isInstanceOf(IllegalStateException::class.java)
		assertThat(cause.message).contains("Expected state.public to be String")
	}

	@Test
	fun `toEvents throws IllegalStateException when public is a map`() = runTest {
		val logs = listOf(ssmLog(0, mapOf("key" to "value")))

		val exception = assertThrows<Exception> {
			invokeToEvents(createPersister(), logs).toList()
		}
		val cause = exception.cause ?: exception
		assertThat(cause).isInstanceOf(IllegalStateException::class.java)
	}

	@Test
	fun `toEvents sorts logs by iteration before deserializing`() = runTest {
		val event1 = TestEvent("id-1", "first")
		val event2 = TestEvent("id-1", "second")
		// Intentionally reversed order
		val logs = listOf(
			ssmLog(1, json.encodeToString(TestEvent.serializer(), event2)),
			ssmLog(0, json.encodeToString(TestEvent.serializer(), event1)),
		)

		val result = invokeToEvents(createPersister(), logs).toList()

		assertThat(result).hasSize(2)
		assertThat(result[0].data).isEqualTo("first")
		assertThat(result[1].data).isEqualTo("second")
	}

	@Test
	fun `toEvents handles empty log list`() = runTest {
		val result = invokeToEvents(createPersister(), emptyList()).toList()
		assertThat(result).isEmpty()
	}

	@Test
	fun `buildSessionName without versioning returns plain id`() {
		val persister = createPersister()
		persister.versioning = false
		val result = invokeBuildSessionName(persister, "event-123")
		assertThat(result).isEqualTo("event-123")
	}

	@Test
	fun `buildSessionName with versioning returns prefixed name`() {
		val persister = createPersister()
		persister.versioning = true
		val result = invokeBuildSessionName(persister, "event-123")
		assertThat(result).isEqualTo("TestAutomate-event-123")
	}

	private fun invokeBuildSessionName(
		persister: EventPersisterSsm<TestEvent, String>,
		id: String
	): String {
		val method = EventPersisterSsm::class.java.getDeclaredMethod("buildSessionName", Any::class.java)
		method.isAccessible = true
		return method.invoke(persister, id) as String
	}

	private fun automateWithTransitions(): S2Automate {
		return S2Automate(
			name = "TestAutomate",
			version = "1.0",
			transitions = arrayOf(
				S2Transition(
					S2StateValue("Init", 0),
					S2StateValue("Active", 1),
					S2RoleValue("Admin"),
					S2TransitionValue("Create"),
					null,
				)
			)
		)
	}
}
