package s2.spring.automate.ssm.persister

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.persist.ErrorCategory
import s2.automate.core.persist.LoadOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2Command
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
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

/**
 * Tests the per-id classification done by [SsmAutomatePersister.loadWithOutcomes].
 *
 * The override classifies each session into [LoadOutcome.Loaded] /
 * [LoadOutcome.Rejected] / [LoadOutcome.Transient] WITHOUT throwing — so a
 * single bad session in a batch surfaces as one Rejected outcome instead of
 * aborting the whole chunk (the originally-broken SSM scale-test scenario).
 *
 * Legacy [SsmAutomatePersister.load] is now implemented in terms of
 * loadWithOutcomes; the last two tests pin its backward-compatible semantics:
 * null for not-found, throw for read errors.
 */
class SsmAutomatePersisterLoadOutcomesTest {

	// --- domain fixtures ---

	interface TestState : s2.dsl.automate.S2State { override val position: Int }

	data class SimpleEntity(
		val id: String,
		val status: Int,
	) : WithS2Id<String>, WithS2State<TestState> {
		override fun s2Id() = id
		override fun s2State() = object : TestState { override val position = status }
	}

	/** Marker event type — not exercised by load, just required by the persister generic. */
	data class TestEvt(val id: String = "") : Event

	data class TestCommand(override val id: String) : S2Command<String>

	private val testAutomate: S2Automate = S2Automate(
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

	private val automateContext = AutomateContext(automate = testAutomate, batch = S2BatchProperties())

	// --- builders for SSM query stubs ---

	private fun ssmLog(sessionName: String, txId: String, iteration: Int, public: String = """{"id":"$sessionName","status":1}"""): SsmSessionStateLog =
		SsmSessionStateLog(
			txId = txId,
			state = SsmSessionState(
				ssm = "test-ssm",
				session = sessionName,
				roles = mapOf("admin" to "Admin"),
				public = public,
				private = emptyMap(),
				origin = SsmTransition(from = 0, to = 1, role = "Admin", action = "Create"),
				current = 1,
				iteration = iteration,
			)
		)

	private fun sessionResult(sessionName: String, logs: List<SsmSessionStateLog>): SsmGetSessionLogsQueryResult =
		SsmGetSessionLogsQueryResult(
			ssmName = "test-ssm",
			sessionName = sessionName,
			logs = logs,
		)

	/**
	 * Build a persister wired to the supplied logs-query stub. Start + perform F2
	 * functions are unused by [loadWithOutcomes] / [load] — wire them to fail so
	 * the test fails loudly if the persister calls them by accident.
	 */
	private fun persisterWith(
		logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction,
	): SsmAutomatePersister<TestState, String, SimpleEntity, TestEvt> =
		SsmAutomatePersister(
			ssmSessionStartFunction = F2Function { _ -> error("start must not be called by load") },
			ssmSessionPerformActionFunction = F2Function { _ -> error("perform must not be called by load") },
			ssmGetSessionLogsQueryFunction = logsQuery,
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			entityType = SimpleEntity::class.java,
			agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
			objectMapper = jacksonObjectMapper(),
			batch = S2BatchProperties(),
		)

	// --- tests ---

	@Test
	fun `loadWithOutcomes - all sessions have logs - emits Loaded per id`() = runTest {
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				queries.toList().map { q ->
					sessionResult(q.sessionName, listOf(ssmLog(q.sessionName, "tx-${q.sessionName}", iteration = 0)))
				}.asFlow()
			}
		val persister = persisterWith(logsQuery)

		val outcomes = persister.loadWithOutcomes(automateContext, flowOf("sess-1", "sess-2", "sess-3")).toList()

		assertThat(outcomes).hasSize(3)
		assertThat(outcomes.map { it.id }).containsExactly("sess-1", "sess-2", "sess-3")
		outcomes.forEach { outcome ->
			val loaded = assertThat(outcome).isInstanceOf(LoadOutcome.Loaded::class.java)
			loaded.extracting { (it as LoadOutcome.Loaded<*, *>).entity }
				.extracting { (it as SimpleEntity).id }
				.isEqualTo(outcome.id)
		}
	}

	@Test
	fun `loadWithOutcomes - missing session emits Rejected SESSION_NOT_FOUND for that id only`() = runTest {
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				// Drop sess-2 from the result set — simulates a session not on chain.
				queries.toList()
					.filter { it.sessionName != "sess-2" }
					.map { q -> sessionResult(q.sessionName, listOf(ssmLog(q.sessionName, "tx", 0))) }
					.asFlow()
			}
		val persister = persisterWith(logsQuery)

		val outcomes = persister.loadWithOutcomes(automateContext, flowOf("sess-1", "sess-2", "sess-3")).toList()

		assertThat(outcomes).hasSize(3)
		val byId = outcomes.associateBy { it.id }

		assertThat(byId["sess-1"]).isInstanceOf(LoadOutcome.Loaded::class.java)
		assertThat(byId["sess-3"]).isInstanceOf(LoadOutcome.Loaded::class.java)

		val rejected = byId["sess-2"] as LoadOutcome.Rejected<*, *>
		assertThat(rejected.error.type).isEqualTo("SESSION_NOT_FOUND")
		assertThat(rejected.category).isEqualTo(ErrorCategory.Rejected)
		assertThat(rejected.error.description).contains("sess-2")
		assertThat(rejected.error.payload).containsEntry("id", "sess-2")
	}

	@Test
	fun `loadWithOutcomes - empty logs emits Rejected SESSION_NOT_INITIALIZED`() = runTest {
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				queries.toList().map { q ->
					// Session exists on chain but has no logs (not yet initialised).
					sessionResult(q.sessionName, emptyList())
				}.asFlow()
			}
		val persister = persisterWith(logsQuery)

		val outcomes = persister.loadWithOutcomes(automateContext, flowOf("sess-1")).toList()

		assertThat(outcomes).hasSize(1)
		val rejected = outcomes.single() as LoadOutcome.Rejected<*, *>
		assertThat(rejected.error.type).isEqualTo("SESSION_NOT_INITIALIZED")
		assertThat(rejected.category).isEqualTo(ErrorCategory.Rejected)
		assertThat(rejected.error.description).contains("sess-1")
		assertThat(rejected.error.payload).containsEntry("id", "sess-1")
	}

	@Test
	fun `loadWithOutcomes - chaincode query throws emits Transient for every id`() = runTest {
		val cause = RuntimeException("peer unreachable")
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { _ -> flow { throw cause } }
		val persister = persisterWith(logsQuery)

		val outcomes = persister.loadWithOutcomes(automateContext, flowOf("sess-1", "sess-2", "sess-3")).toList()

		// Whole query failed — can't classify per id, so every id surfaces as Transient
		// (retryable; the next call may succeed when the peer is reachable again).
		assertThat(outcomes).hasSize(3)
		assertThat(outcomes.map { it.id }).containsExactly("sess-1", "sess-2", "sess-3")
		outcomes.forEach { outcome ->
			val transient = outcome as LoadOutcome.Transient<*, *>
			assertThat(transient.error.type).isEqualTo("CHAINCODE_QUERY_FAILED")
			assertThat(transient.category).isEqualTo(ErrorCategory.Transient)
			assertThat(transient.error.cause).isSameAs(cause)
		}
	}

	@Test
	fun `loadWithOutcomes - JSON parse fails for one log emits Rejected DESERIALIZATION_FAILED for that id only`() = runTest {
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				queries.toList().map { q ->
					// sess-2 has a corrupt log payload — Jackson will fail to parse it.
					val public = if (q.sessionName == "sess-2") "not-valid-json" else """{"id":"${q.sessionName}","status":1}"""
					sessionResult(q.sessionName, listOf(ssmLog(q.sessionName, "tx", 0, public)))
				}.asFlow()
			}
		val persister = persisterWith(logsQuery)

		val outcomes = persister.loadWithOutcomes(automateContext, flowOf("sess-1", "sess-2", "sess-3")).toList()

		assertThat(outcomes).hasSize(3)
		val byId = outcomes.associateBy { it.id }

		assertThat(byId["sess-1"]).isInstanceOf(LoadOutcome.Loaded::class.java)
		assertThat(byId["sess-3"]).isInstanceOf(LoadOutcome.Loaded::class.java)

		val rejected = byId["sess-2"] as LoadOutcome.Rejected<*, *>
		assertThat(rejected.error.type).isEqualTo("DESERIALIZATION_FAILED")
		// Permanent (Rejected) — bad on-chain payload won't fix itself, no point retrying.
		assertThat(rejected.category).isEqualTo(ErrorCategory.Rejected)
		assertThat(rejected.error.payload).containsEntry("id", "sess-2")
		assertThat(rejected.error.cause).isNotNull
	}

	@Test
	fun `loadWithOutcomes - mixed batch with Loaded + Rejected emits per-id outcomes in input order`() = runTest {
		// sess-2 missing from chain (not in query response), sess-4 has empty logs.
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				queries.toList()
					.filter { it.sessionName != "sess-2" }
					.map { q ->
						val logs = if (q.sessionName == "sess-4") emptyList() else listOf(ssmLog(q.sessionName, "tx", 0))
						sessionResult(q.sessionName, logs)
					}.asFlow()
			}
		val persister = persisterWith(logsQuery)

		val outcomes = persister.loadWithOutcomes(automateContext, flowOf("sess-1", "sess-2", "sess-3", "sess-4")).toList()

		assertThat(outcomes).hasSize(4)
		assertThat(outcomes.map { it.id }).containsExactly("sess-1", "sess-2", "sess-3", "sess-4")

		assertThat(outcomes[0]).isInstanceOf(LoadOutcome.Loaded::class.java)
		val r2 = outcomes[1] as LoadOutcome.Rejected<*, *>
		assertThat(r2.error.type).isEqualTo("SESSION_NOT_FOUND")
		assertThat(outcomes[2]).isInstanceOf(LoadOutcome.Loaded::class.java)
		val r4 = outcomes[3] as LoadOutcome.Rejected<*, *>
		assertThat(r4.error.type).isEqualTo("SESSION_NOT_INITIALIZED")
	}

	@Test
	fun `loadWithOutcomes - empty input emits nothing`() = runTest {
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				// Should not even receive an empty batch — persister should short-circuit.
				queries.toList()
				flowOf()
			}
		val persister = persisterWith(logsQuery)

		val outcomes = persister.loadWithOutcomes(automateContext, flowOf<String>()).toList()

		assertThat(outcomes).isEmpty()
	}

	// --- legacy load() compatibility tests ---

	@Test
	fun `load(Flow) - returns null for not-found sessions (legacy compat)`() = runTest {
		// Pre-fix: this case threw IllegalStateException — killing the whole flow.
		// Post-fix: returns null for the missing id, so legacy callers see a typed
		// nullable instead of an exception.
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				queries.toList()
					.filter { it.sessionName != "sess-2" }
					.map { q -> sessionResult(q.sessionName, listOf(ssmLog(q.sessionName, "tx", 0))) }
					.asFlow()
			}
		val persister = persisterWith(logsQuery)

		val entities = persister.load(automateContext, flowOf("sess-1", "sess-2", "sess-3")).toList()

		assertThat(entities).hasSize(3)
		assertThat(entities[0]).isNotNull
		assertThat(entities[0]?.id).isEqualTo("sess-1")
		assertThat(entities[1]).isNull()  // sess-2 not on chain → null, not throw
		assertThat(entities[2]).isNotNull
		assertThat(entities[2]?.id).isEqualTo("sess-3")
	}

	@Test
	fun `load(Flow) - throws for Transient read errors (legacy compat)`() = runTest {
		val cause = RuntimeException("peer unreachable")
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { _ -> flow { throw cause } }
		val persister = persisterWith(logsQuery)

		val thrown = assertThrows<RuntimeException> {
			persister.load(automateContext, flowOf("sess-1")).toList()
		}
		// The original cause is preserved when present, so callers can introspect it.
		assertThat(thrown).isSameAs(cause)
	}

	@Test
	fun `loadWithOutcomes - chaincode query throws CancellationException - propagates without conversion`() = runTest {
		// CancellationException MUST NOT be caught by the chain-query catch block,
		// otherwise cooperative cancellation gets silently converted into Transient
		// outcomes and the parent coroutine never sees the cancel signal.
		val cancel = CancellationException("cooperative cancel")
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { _ -> flow { throw cancel } }
		val persister = persisterWith(logsQuery)

		val thrown = assertThrows<CancellationException> {
			persister.loadWithOutcomes(automateContext, flowOf("sess-1")).toList()
		}
		assertThat(thrown).isSameAs(cancel)
	}

	@Test
	fun `loadWithOutcomes - JSON parse fails with CancellationException - propagates without conversion`() = runTest {
		// The Jackson deserialiser is unlikely to throw CancellationException
		// directly, but a custom deserialiser could call into suspending code.
		// We pin the behaviour: the per-id JSON-parse catch must let cancellation
		// through too.
		val cancel = CancellationException("cooperative cancel during parse")
		val cancellingMapper = object : ObjectMapper() {
			override fun <T> readValue(content: String, valueType: Class<T>): T {
				throw cancel
			}
		}
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				queries.toList().map { q -> sessionResult(q.sessionName, listOf(ssmLog(q.sessionName, "tx", 0))) }.asFlow()
			}
		val persister = SsmAutomatePersister<TestState, String, SimpleEntity, TestEvt>(
			ssmSessionStartFunction = F2Function { _ -> error("start must not be called") },
			ssmSessionPerformActionFunction = F2Function { _ -> error("perform must not be called") },
			ssmGetSessionLogsQueryFunction = logsQuery,
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			entityType = SimpleEntity::class.java,
			agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
			objectMapper = cancellingMapper,
			batch = S2BatchProperties(),
		)

		val thrown = assertThrows<CancellationException> {
			persister.loadWithOutcomes(automateContext, flowOf("sess-1")).toList()
		}
		assertThat(thrown).isSameAs(cancel)
	}
}
