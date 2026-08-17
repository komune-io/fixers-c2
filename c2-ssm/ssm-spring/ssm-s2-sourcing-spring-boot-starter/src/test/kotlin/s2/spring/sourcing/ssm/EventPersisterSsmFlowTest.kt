package s2.spring.sourcing.ssm

import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlinx.coroutines.runBlocking
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
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.toSsmUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
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

/**
 * Unit tests for EventPersisterSsm through its public EventRepository API —
 * load / loadAll / persist(single) / persist(flow) — with stubbed F2 functions,
 * no Spring context and no Fabric.
 */
class EventPersisterSsmFlowTest {

	@Serializable
	data class TestEvent(
		val eventId: String,
		val data: String,
	) : Evt, WithS2Id<String> {
		override fun s2Id() = eventId
	}

	private val json = Json { ignoreUnknownKeys = true }
	private val chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")
	private val agent = Agent(name = "signer", pub = ByteArray(0))

	private val automate = S2Automate(
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

	private fun encode(event: TestEvent) = json.encodeToString(TestEvent.serializer(), event)

	private fun ssmLog(sessionName: String, iteration: Int, public: Any?) = SsmSessionStateLog(
		txId = "tx-$sessionName-$iteration",
		state = SsmSessionState(
			ssm = "TestAutomate",
			session = sessionName,
			roles = mapOf("signer" to "Admin"),
			public = public,
			private = emptyMap(),
			origin = SsmTransition(from = 0, to = 1, role = "Admin", action = "Create"),
			current = 1,
			iteration = iteration,
		)
	)

	private fun logsResult(sessionName: String, logs: List<SsmSessionStateLog>) = SsmGetSessionLogsQueryResult(
		ssmName = "TestAutomate",
		sessionName = sessionName,
		logs = logs,
	)

	private fun dataSession(sessionName: String, iteration: Int) = DataSsmSession(
		sessionName = sessionName,
		state = DataSsmSessionState(
			details = SsmSessionState(
				ssm = "TestAutomate",
				session = sessionName,
				roles = mapOf("signer" to "Admin"),
				public = "{}",
				private = emptyMap(),
				origin = null,
				current = 1,
				iteration = iteration,
			),
			transaction = null,
		),
		channel = DataChannel(id = "sandbox"),
		transaction = null,
		ssmUri = chaincodeUri.toSsmUri("TestAutomate"),
		transactions = emptyList(),
	)

	private fun createPersister(
		startFunction: SsmTxSessionStartFunction = F2Function { error("start not expected") },
		performFunction: SsmTxSessionPerformActionFunction = F2Function { error("perform not expected") },
		getSessionFunction: DataSsmSessionGetQueryFunction = F2Function { error("get session not expected") },
		getLogsFunction: SsmGetSessionLogsQueryFunction = F2Function { error("get logs not expected") },
		listSessionsFunction: DataSsmSessionListQueryFunction = F2Function { error("list sessions not expected") },
		versioning: Boolean = false,
	) = EventPersisterSsm<TestEvent, String>(
		s2Automate = automate,
		eventType = TestEvent::class,
		batchParams = S2BatchProperties(),
		ssmSessionStartFunction = startFunction,
		ssmSessionPerformActionFunction = performFunction,
		dataSsmSessionGetQueryFunction = getSessionFunction,
		ssmGetSessionLogsQueryFunction = getLogsFunction,
		dataSsmSessionListQueryFunction = listSessionsFunction,
		chaincodeUri = chaincodeUri,
		agentSigner = agent,
		json = json,
		versioning = versioning,
	)

	private fun capturingStart(captured: MutableList<SsmStartCommand>): SsmTxSessionStartFunction =
		F2Function { commands ->
			commands.map { cmd ->
				captured += cmd
				CommandOutcome(outcome = "Committed", msgId = cmd.msgId, transactionId = "tx-${cmd.msgId}")
			}
		}

	private fun capturingPerform(captured: MutableList<SsmPerformCommand>): SsmTxSessionPerformActionFunction =
		F2Function { commands ->
			commands.map { cmd ->
				captured += cmd
				CommandOutcome(outcome = "Committed", msgId = cmd.msgId, transactionId = "tx-${cmd.msgId}")
			}
		}

	// ------------------------------------------------------------------
	// persist(single event)
	// ------------------------------------------------------------------

	@Test
	suspend fun `persist single event starts a session when the chain has no iteration`() {
		val started = mutableListOf<SsmStartCommand>()
		val persister = createPersister(
			startFunction = capturingStart(started),
			getSessionFunction = F2Function { queries -> queries.map { DataSsmSessionGetQueryResult(null) } },
		)
		val event = TestEvent("id-1", "hello")

		val returned = persister.persist(event)

		assertThat(returned).isEqualTo(event)
		assertThat(started).hasSize(1)
		val command = started.single()
		assertThat(command.session.ssm).isEqualTo("TestAutomate")
		assertThat(command.session.session).isEqualTo("id-1")
		assertThat(command.session.roles).isEqualTo(mapOf("signer" to "Admin"))
		assertThat(command.session.public).isEqualTo(encode(event))
		assertThat(command.session.private).isEmpty()
		assertThat(command.signerName).isEqualTo("signer")
		assertThat(command.chaincodeUri).isEqualTo(chaincodeUri)
		assertThat(command.msgId).isNotBlank()
	}

	@Test
	suspend fun `persist single event performs an action at the chain iteration when the session exists`() {
		val performed = mutableListOf<SsmPerformCommand>()
		val persister = createPersister(
			performFunction = capturingPerform(performed),
			getSessionFunction = F2Function { queries ->
				queries.map { DataSsmSessionGetQueryResult(dataSession("id-1", iteration = 3)) }
			},
		)
		val event = TestEvent("id-1", "updated")

		persister.persist(event)

		assertThat(performed).hasSize(1)
		val command = performed.single()
		assertThat(command.action).isEqualTo("TestEvent")
		assertThat(command.context.session).isEqualTo("id-1")
		assertThat(command.context.iteration).isEqualTo(3)
		assertThat(command.context.public).isEqualTo(encode(event))
		assertThat(command.signerName).isEqualTo("signer")
	}

	@Test
	suspend fun `persist single event with versioning prefixes the session name with the automate name`() {
		val started = mutableListOf<SsmStartCommand>()
		val persister = createPersister(
			startFunction = capturingStart(started),
			getSessionFunction = F2Function { queries -> queries.map { DataSsmSessionGetQueryResult(null) } },
			versioning = true,
		)

		persister.persist(TestEvent("id-1", "hello"))

		assertThat(started.single().session.session).isEqualTo("TestAutomate-id-1")
	}

	// ------------------------------------------------------------------
	// persist(flow)
	// ------------------------------------------------------------------

	@Test
	suspend fun `persist flow starts sessions without logs and performs actions on sessions with logs`() {
		val started = mutableListOf<SsmStartCommand>()
		val performed = mutableListOf<SsmPerformCommand>()
		val newEvent = TestEvent("new-1", "created")
		val existingEvent = TestEvent("old-1", "updated")

		val persister = createPersister(
			startFunction = capturingStart(started),
			performFunction = capturingPerform(performed),
			getLogsFunction = F2Function { queries ->
				queries.map { query ->
					when (query.sessionName) {
						"new-1" -> logsResult("new-1", emptyList())
						else -> logsResult("old-1", listOf(
							ssmLog("old-1", 0, encode(existingEvent)),
							ssmLog("old-1", 2, encode(existingEvent)),
						))
					}
				}
			},
		)

		val returned = persister.persist(flowOf(newEvent, existingEvent)).toList()

		assertThat(returned).containsExactlyInAnyOrder(newEvent, existingEvent)
		assertThat(started).hasSize(1)
		assertThat(started.single().session.session).isEqualTo("new-1")
		assertThat(started.single().session.public).isEqualTo(encode(newEvent))
		assertThat(performed).hasSize(1)
		assertThat(performed.single().context.session).isEqualTo("old-1")
		assertThat(performed.single().context.iteration).isEqualTo(2)
	}

	@Test
	suspend fun `persist flow ignores chain results for unknown session names`() {
		val started = mutableListOf<SsmStartCommand>()
		val event = TestEvent("new-1", "created")

		val persister = createPersister(
			startFunction = capturingStart(started),
			getLogsFunction = F2Function { queries ->
				queries.toList()
				flowOf(
					logsResult("new-1", emptyList()),
					logsResult("ghost", emptyList()),
				)
			},
		)

		val returned = persister.persist(flowOf(event)).toList()

		assertThat(returned).containsExactly(event)
		assertThat(started).hasSize(1)
		assertThat(started.single().session.session).isEqualTo("new-1")
	}

	@Test
	fun `persist flow rejects duplicated event ids due to SSM limitations`() {
		val persister = createPersister(
			getLogsFunction = F2Function { queries -> queries.map { logsResult(it.sessionName, emptyList()) } },
		)
		val events = flowOf(TestEvent("dup-1", "a"), TestEvent("dup-1", "b"))

		assertThatThrownBy { runBlocking { persister.persist(events).toList() } }
			.hasMessageContaining("Duplicate events detected")
			.hasMessageContaining("dup-1")
	}

	// ------------------------------------------------------------------
	// load / loadAll / createTable
	// ------------------------------------------------------------------

	@Test
	suspend fun `load fetches the session logs and replays events sorted by iteration`() {
		val seenQueries = mutableListOf<SsmGetSessionLogsQuery>()
		val first = TestEvent("id-1", "first")
		val second = TestEvent("id-1", "second")
		val persister = createPersister(
			getLogsFunction = F2Function { queries ->
				queries.map { query ->
					seenQueries += query
					logsResult(query.sessionName, listOf(
						ssmLog(query.sessionName, 1, encode(second)),
						ssmLog(query.sessionName, 0, encode(first)),
					))
				}
			},
		)

		val events = persister.load("id-1").toList()

		assertThat(events).containsExactly(first, second)
		assertThat(seenQueries).hasSize(1)
		assertThat(seenQueries.single().sessionName).isEqualTo("id-1")
		assertThat(seenQueries.single().ssmName).isEqualTo("TestAutomate")
		assertThat(seenQueries.single().chaincodeUri).isEqualTo(chaincodeUri)
	}

	@Test
	suspend fun `loadAll lists the sessions and replays every event sorted by iteration`() {
		val eventA0 = TestEvent("a", "a0")
		val eventB0 = TestEvent("b", "b0")
		val eventB1 = TestEvent("b", "b1")
		val persister = createPersister(
			listSessionsFunction = F2Function { queries ->
				queries.map {
					DataSsmSessionListQueryResult(listOf(dataSession("a", 0), dataSession("b", 1)))
				}
			},
			getLogsFunction = F2Function { queries ->
				queries.map { query ->
					when (query.sessionName) {
						"a" -> logsResult("a", listOf(ssmLog("a", 0, encode(eventA0))))
						else -> logsResult("b", listOf(
							ssmLog("b", 1, encode(eventB1)),
							ssmLog("b", 0, encode(eventB0)),
						))
					}
				}
			},
		)

		val events = persister.loadAll().toList()

		assertThat(events).hasSize(3)
		assertThat(events.filter { it.eventId == "b" }).containsExactly(eventB0, eventB1)
	}

	@Test
	suspend fun `createTable is a no-op`() {
		createPersister().createTable()
	}

	// ------------------------------------------------------------------
	// ExecutableAction
	// ------------------------------------------------------------------

	@Test
	fun `ExecutableAction classifies a missing iteration as CREATE and a present one as UPDATE`() {
		assertThat(ExecutableAction(TestEvent("id", "x"), iteration = null).action).isEqualTo(Action.CREATE)
		assertThat(ExecutableAction(TestEvent("id", "x"), iteration = 0).action).isEqualTo(Action.UPDATE)
	}
}
