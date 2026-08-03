package s2.spring.sourcing.ssm

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import s2.automate.core.config.S2BatchProperties
import s2.dsl.automate.Evt
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2RoleValue
import s2.dsl.automate.S2StateValue
import s2.dsl.automate.S2Transition
import s2.dsl.automate.S2TransitionValue
import s2.dsl.automate.model.WithS2Id
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.SsmTransition
import ssm.chaincode.dsl.model.uri.toSsmUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionGetQueryResult
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQueryResult
import ssm.data.dsl.model.DataChannel
import ssm.data.dsl.model.DataSsmSession
import ssm.data.dsl.model.DataSsmSessionState
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.dsl.CommandOutcome

class EventPersisterSsmRepositoryTest {

	@Serializable
	data class TestEvent(
		val eventId: String,
		val data: String,
	) : Evt, WithS2Id<String> {
		override fun s2Id() = eventId
	}

	private val json = Json { ignoreUnknownKeys = true }
	private val chaincodeUri = ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm")
	private val signer = Agent(name = "admin", pub = "pub-key".toByteArray())

	private val startedCommands = CopyOnWriteArrayList<SsmStartCommand>()
	private val performedCommands = CopyOnWriteArrayList<SsmPerformCommand>()

	private val sessionLogs = mutableMapOf<String, List<SsmSessionStateLog>>()
	private val existingIterations = mutableMapOf<String, Int>()

	private fun automate() = S2Automate(
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

	private fun encoded(event: TestEvent) = json.encodeToString(TestEvent.serializer(), event)

	private fun log(session: String, iteration: Int, event: TestEvent) = SsmSessionStateLog(
		txId = "tx-$session-$iteration",
		state = SsmSessionState(
			ssm = "TestAutomate",
			session = session,
			roles = mapOf("admin" to "Admin"),
			public = encoded(event),
			private = emptyMap(),
			origin = SsmTransition(from = 0, to = 1, role = "Admin", action = "Create"),
			current = 1,
			iteration = iteration,
		)
	)

	private fun dataSession(session: String, iteration: Int): DataSsmSession {
		val event = TestEvent(session, "data-$iteration")
		return DataSsmSession(
			sessionName = session,
			state = DataSsmSessionState(
				details = log(session, iteration, event).state,
				transaction = null,
			),
			channel = DataChannel("sandbox"),
			transaction = null,
			ssmUri = chaincodeUri.toSsmUri("TestAutomate"),
			transactions = emptyList(),
		)
	}

	private fun createPersister(): EventPersisterSsm<TestEvent, String> {
		val persister = EventPersisterSsm<TestEvent, String>(
			s2Automate = automate(),
			eventType = TestEvent::class,
			batchParams = S2BatchProperties(),
		)
		persister.json = json
		persister.chaincodeUri = chaincodeUri
		persister.agentSigner = signer
		persister.versioning = false
		persister.ssmSessionStartFunction = SsmTxSessionStartFunction { msgs ->
			msgs.map { cmd ->
				startedCommands.add(cmd)
				CommandOutcome(outcome = "Committed", msgId = cmd.msgId)
			}
		}
		persister.ssmSessionPerformActionFunction = SsmTxSessionPerformActionFunction { msgs ->
			msgs.map { cmd ->
				performedCommands.add(cmd)
				CommandOutcome(outcome = "Committed", msgId = cmd.msgId)
			}
		}
		persister.ssmGetSessionLogsQueryFunction = SsmGetSessionLogsQueryFunction { msgs ->
			msgs.map { query ->
				SsmGetSessionLogsQueryResult(
					ssmName = query.ssmName,
					sessionName = query.sessionName,
					logs = sessionLogs[query.sessionName].orEmpty(),
				)
			}
		}
		persister.dataSsmSessionGetQueryFunction = DataSsmSessionGetQueryFunction { msgs ->
			msgs.map { query ->
				DataSsmSessionGetQueryResult(
					item = existingIterations[query.sessionName]?.let { iteration ->
						dataSession(query.sessionName, iteration)
					}
				)
			}
		}
		persister.dataSsmSessionListQueryFunction = DataSsmSessionListQueryFunction { msgs ->
			msgs.map {
				DataSsmSessionListQueryResult(
					items = sessionLogs.keys.map { session ->
						dataSession(session, 0)
					}
				)
			}
		}
		return persister
	}

	@Test
	suspend fun `load returns the events of the session sorted by iteration`() {
		val first = TestEvent("id-1", "first")
		val second = TestEvent("id-1", "second")
		sessionLogs["id-1"] = listOf(
			log("id-1", 1, second),
			log("id-1", 0, first),
		)

		val events = createPersister().load("id-1").toList()

		assertThat(events).containsExactly(first, second)
	}

	@Test
	suspend fun `load returns empty flow when the session has no log`() {
		val events = createPersister().load("unknown").toList()
		assertThat(events).isEmpty()
	}

	@Test
	suspend fun `loadAll aggregates the events of every session`() {
		val event1 = TestEvent("id-1", "one")
		val event2 = TestEvent("id-2", "two")
		sessionLogs["id-1"] = listOf(log("id-1", 0, event1))
		sessionLogs["id-2"] = listOf(log("id-2", 1, event2))

		val events = createPersister().loadAll().toList()

		assertThat(events).containsExactly(event1, event2)
	}

	@Test
	suspend fun `createTable is a no-op`() {
		createPersister().createTable()
	}

	@Test
	suspend fun `persist single event starts a session when none exists`() {
		val event = TestEvent("id-9", "created")

		val returned = createPersister().persist(event)

		assertThat(returned).isEqualTo(event)
		assertThat(startedCommands).hasSize(1)
		val command = startedCommands.first()
		assertThat(command.session.ssm).isEqualTo("TestAutomate")
		assertThat(command.session.session).isEqualTo("id-9")
		assertThat(command.session.public).isEqualTo(encoded(event))
		assertThat(command.session.roles).containsKey("admin")
		assertThat(command.signerName).isEqualTo("admin")
		assertThat(command.chaincodeUri.uri).isEqualTo(chaincodeUri.uri)
		assertThat(performedCommands).isEmpty()
	}

	@Test
	suspend fun `persist single event performs an action when the session exists`() {
		existingIterations["id-5"] = 3
		val event = TestEvent("id-5", "updated")

		val returned = createPersister().persist(event)

		assertThat(returned).isEqualTo(event)
		assertThat(performedCommands).hasSize(1)
		val command = performedCommands.first()
		assertThat(command.action).isEqualTo("TestEvent")
		assertThat(command.context.session).isEqualTo("id-5")
		assertThat(command.context.iteration).isEqualTo(3)
		assertThat(command.context.public).isEqualTo(encoded(event))
		assertThat(command.signerName).isEqualTo("admin")
		assertThat(startedCommands).isEmpty()
	}

	@Test
	suspend fun `persist flow starts sessions for new ids and performs actions on known ids`() {
		val created = TestEvent("id-new", "created")
		val updated = TestEvent("id-known", "updated")
		sessionLogs["id-known"] = listOf(log("id-known", 2, updated))

		val returned = createPersister().persist(flowOf(created, updated)).toList()

		assertThat(returned).containsExactlyInAnyOrder(created, updated)
		assertThat(startedCommands).hasSize(1)
		assertThat(startedCommands.first().session.session).isEqualTo("id-new")
		assertThat(performedCommands).hasSize(1)
		val perform = performedCommands.first()
		assertThat(perform.context.session).isEqualTo("id-known")
		assertThat(perform.context.iteration).isEqualTo(2)
		assertThat(perform.action).isEqualTo("TestEvent")
	}

	@Test
	suspend fun `persist flow rejects duplicated event identifiers`() {
		val event1 = TestEvent("dup", "one")
		val event2 = TestEvent("dup", "two")
		val persister = createPersister()

		val exception = runCatching {
			persister.persist(flowOf(event1, event2)).toList()
		}.exceptionOrNull()

		assertThat(exception).isNotNull()
		val cause = generateSequence(exception) { it.cause }.last()
		assertThat(cause).isInstanceOf(IllegalArgumentException::class.java)
		assertThat(cause.message).contains("Duplicate events detected")
	}

	@Test
	suspend fun `persist single event uses versioned session name when versioning is enabled`() {
		val persister = createPersister()
		persister.versioning = true
		val event = TestEvent("id-7", "created")

		persister.persist(event)

		assertThat(startedCommands).hasSize(1)
		assertThat(startedCommands.first().session.session).isEqualTo("TestAutomate-id-7")
	}
}
