package s2.spring.automate.ssm.persister

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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
 * Unit tests (no Spring, no Fabric) for the SsmAutomatePersister paths not pinned by the
 * existing persister tests:
 *  - the legacy [SsmAutomatePersister.load] / [SsmAutomatePersister.persistInit] /
 *    [SsmAutomatePersister.persist] wrappers around the *WithOutcomes methods;
 *  - the NO_LOGS rejection in the chain-side iteration lookup;
 *  - MISSING_OUTCOME / UNKNOWN_OUTCOME mapping in toPersistOutcome;
 *  - idempotent reconciliation: promotion when the chain already holds the intended
 *    state, fallback when the reconciliation query fails, and the Transient exclusion.
 */
class SsmAutomatePersisterReconcileAndWrapperTest {

	interface TestState : s2.dsl.automate.S2State {
		override val position: Int
	}

	data class SimpleEntity(
		val id: String,
		val status: Int,
	) : WithS2Id<String>, WithS2State<TestState> {
		override fun s2Id() = id
		override fun s2State() = object : TestState { override val position = status }
	}

	data class IterableEntity(
		val id: String,
		val status: Int,
		val iteration: Int,
	) : WithS2Id<String>, WithS2State<TestState>, WithS2Iteration {
		override fun s2Id() = id
		override fun s2State() = object : TestState { override val position = status }
		override fun s2Iteration() = iteration
		override fun withS2Iteration(iteration: Int) = copy(iteration = iteration)
	}

	data class TestEvt(val id: String = "") : Event

	data class TestCommand(override val id: String) : S2Command<String>

	data class TestInitCommand(val entityId: String) : S2InitCommand

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

	/** Every transition carries a result — S2Automate.withResultAsAction resolves to true. */
	private val resultAsActionAutomate: S2Automate = S2Automate(
		name = "test-ssm",
		version = null,
		transitions = arrayOf(
			S2Transition(
				from = S2StateValue(name = "StateOne", position = 1),
				to = S2StateValue(name = "StateTwo", position = 2),
				role = S2RoleValue(name = "TestRole"),
				action = S2TransitionValue(name = "TestCommand"),
				result = S2TransitionValue(name = "TestEvt"),
			)
		),
	)

	private val objectMapper = ObjectMapper()

	private fun makeTransitionContext(
		entity: IterableEntity,
		automate: S2Automate = testAutomate,
	): TransitionAppliedContext<TestState, String, IterableEntity, TestEvt, S2Automate> =
		TransitionAppliedContext(
			automateContext = AutomateContext(automate = automate, batch = S2BatchProperties()),
			msgId = entity.id,
			from = object : TestState { override val position = entity.status },
			msg = TestCommand(id = entity.id),
			event = TestEvt(id = entity.id),
			entity = entity,
		)

	private fun makeSimpleTransitionContext(
		entity: SimpleEntity,
	): TransitionAppliedContext<TestState, String, SimpleEntity, TestEvt, S2Automate> =
		TransitionAppliedContext(
			automateContext = AutomateContext(automate = testAutomate, batch = S2BatchProperties()),
			msgId = entity.id,
			from = object : TestState { override val position = entity.status },
			msg = TestCommand(id = entity.id),
			event = TestEvt(id = entity.id),
			entity = entity,
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

	private fun scriptedPerform(
		outcomeFor: (msgId: String) -> CommandOutcome?,
	): SsmTxSessionPerformActionFunction = F2Function { commands ->
		commands.toList().mapNotNull { cmd -> outcomeFor(cmd.msgId) }.asFlow()
	}

	private fun buildPersister(
		perform: SsmTxSessionPerformActionFunction = F2Function { _ -> error("perform not expected") },
		start: SsmTxSessionStartFunction = F2Function { _ -> error("start not expected") },
		logs: SsmGetSessionLogsQueryFunction = F2Function { _ -> flowOf<SsmGetSessionLogsQueryResult>() },
	): SsmAutomatePersister<TestState, String, IterableEntity, TestEvt> = SsmAutomatePersister(
		ssmSessionStartFunction = start,
		ssmSessionPerformActionFunction = perform,
		ssmGetSessionLogsQueryFunction = logs,
		chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
		entityType = IterableEntity::class.java,
		agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
		objectMapper = objectMapper,
		batch = S2BatchProperties(),
	)

	private fun ssmLog(txId: String, iteration: Int, public: Any? = "{}") = SsmSessionStateLog(
		txId = txId,
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

	// ------------------------------------------------------------------
	// Legacy wrappers
	// ------------------------------------------------------------------

	@Test
	suspend fun `load by single id returns null when the session is not on chain`() {
		val persister = buildPersister()

		val loaded = persister.load(
			AutomateContext(automate = testAutomate, batch = S2BatchProperties()),
			"missing-id",
		)

		assertThat(loaded).isNull()
	}

	@Test
	suspend fun `persistInit keeps committed events and drops failures`() {
		val start: SsmTxSessionStartFunction = F2Function { commands ->
			commands.map { cmd ->
				if (cmd.msgId == "start:ok") {
					CommandOutcome(outcome = "Committed", msgId = cmd.msgId, transactionId = "tx-1", blockNumber = 1L)
				} else {
					CommandOutcome(outcome = "Transient", msgId = cmd.msgId, errorCode = "GRPC_UNAVAILABLE")
				}
			}
		}
		val persister = buildPersister(start = start)
		val ok = makeInitTransitionContext(IterableEntity("ok", 1, 0))
		val down = makeInitTransitionContext(IterableEntity("down", 1, 0))

		val events = persister.persistInit(flowOf(ok, down)).toList()

		assertThat(events).containsExactly(TestEvt(id = "ok"))
	}

	@Test
	suspend fun `persist keeps committed events and drops failures`() {
		val perform = scriptedPerform { msgId ->
			if (msgId.startsWith("ok:")) {
				CommandOutcome(outcome = "Committed", msgId = msgId, transactionId = "tx-1", blockNumber = 1L)
			} else {
				CommandOutcome(outcome = "Transient", msgId = msgId, errorCode = "GRPC_UNAVAILABLE")
			}
		}
		val persister = buildPersister(perform = perform)
		val ok = makeTransitionContext(IterableEntity("ok", 1, 0))
		val down = makeTransitionContext(IterableEntity("down", 1, 0))

		val events = persister.persist(flowOf(ok, down)).toList()

		assertThat(events).containsExactly(TestEvt(id = "ok"))
	}

	// ------------------------------------------------------------------
	// Chain-side iteration lookup — NO_LOGS
	// ------------------------------------------------------------------

	@Test
	suspend fun `persistWithOutcomes rejects sessions that exist on chain but have no logs`() {
		val logs: SsmGetSessionLogsQueryFunction = F2Function { queries ->
			queries.map { query ->
				SsmGetSessionLogsQueryResult(ssmName = "test-ssm", sessionName = query.sessionName, logs = emptyList())
			}
		}
		val persister = SsmAutomatePersister<TestState, String, SimpleEntity, TestEvt>(
			ssmSessionStartFunction = F2Function { _ -> error("start not expected") },
			ssmSessionPerformActionFunction = F2Function { commands -> commands.map { error("perform not expected") } },
			ssmGetSessionLogsQueryFunction = logs,
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			entityType = SimpleEntity::class.java,
			agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
			objectMapper = objectMapper,
			batch = S2BatchProperties(),
		)
		val ctx = makeSimpleTransitionContext(SimpleEntity("sess-1", 1))

		val outcomes = persister.persistWithOutcomes(flowOf(ctx)).toList()

		val rejected = outcomes.single() as PersistOutcome.Rejected<TestEvt>
		assertThat(rejected.error.type).isEqualTo("NO_LOGS")
		assertThat(rejected.msgId).isEqualTo("sess-1:lookup")
	}

	/**
	 * Documents the emission semantics for a mixed batch (one valid context, one failed
	 * iteration lookup): exactly one outcome per input context, correlated by msgId —
	 * NOT by position. Lookup failures are emitted before the chaincode-invoked outcomes
	 * (behavior unchanged since the pre-refactor implementation). The only consumer of
	 * this method, s2's S2AutomateOutcomeEngineImpl, keys outcomes by msgId and wraps
	 * them in random-id envelopes, so emission order carries no contract. If this order
	 * ever becomes load-bearing, this test is the tripwire.
	 */
	@Test
	suspend fun `persistWithOutcomes emits one msgId-correlated outcome per context, lookup failures first`() {
		val logs: SsmGetSessionLogsQueryFunction = F2Function { queries ->
			queries.map { query ->
				val sessionLogs = if (query.sessionName == "ok") listOf(ssmLog("tx-prev", iteration = 1)) else emptyList()
				SsmGetSessionLogsQueryResult(ssmName = "test-ssm", sessionName = query.sessionName, logs = sessionLogs)
			}
		}
		val perform: SsmTxSessionPerformActionFunction = F2Function { commands ->
			commands.map { cmd ->
				CommandOutcome(outcome = "Committed", msgId = cmd.msgId, transactionId = "tx-ok", blockNumber = 2L)
			}
		}
		val persister = SsmAutomatePersister<TestState, String, SimpleEntity, TestEvt>(
			ssmSessionStartFunction = F2Function { _ -> error("start not expected") },
			ssmSessionPerformActionFunction = perform,
			ssmGetSessionLogsQueryFunction = logs,
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			entityType = SimpleEntity::class.java,
			agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
			objectMapper = objectMapper,
			batch = S2BatchProperties(),
		)
		val valid = makeSimpleTransitionContext(SimpleEntity("ok", 1))
		val noLogs = makeSimpleTransitionContext(SimpleEntity("empty", 1))

		val outcomes = persister.persistWithOutcomes(flowOf(valid, noLogs)).toList()

		assertThat(outcomes).hasSize(2)
		val rejected = outcomes[0] as PersistOutcome.Rejected<TestEvt>
		assertThat(rejected.msgId).isEqualTo("empty:lookup")
		assertThat(rejected.error.type).isEqualTo("NO_LOGS")
		val success = outcomes[1] as PersistOutcome.Success<TestEvt>
		assertThat(success.msgId).isEqualTo("ok:2")
		assertThat(success.event).isEqualTo(TestEvt(id = "ok"))
	}

	// ------------------------------------------------------------------
	// toPersistOutcome — MISSING_OUTCOME / UNKNOWN_OUTCOME
	// ------------------------------------------------------------------

	@Test
	suspend fun `a command with no returned outcome maps to Indeterminate MISSING_OUTCOME`() {
		val persister = buildPersister(perform = scriptedPerform { null })
		val ctx = makeTransitionContext(IterableEntity("lost", 1, 0))

		val outcomes = persister.persistWithOutcomes(flowOf(ctx)).toList()

		val indeterminate = outcomes.single() as PersistOutcome.Indeterminate<TestEvt>
		assertThat(indeterminate.error.type).isEqualTo("MISSING_OUTCOME")
		assertThat(indeterminate.msgId).isEqualTo("lost:1")
	}

	@Test
	suspend fun `an unknown outcome string maps to Indeterminate UNKNOWN_OUTCOME`() {
		val persister = buildPersister(
			perform = scriptedPerform { msgId -> CommandOutcome(outcome = "Weird", msgId = msgId) },
		)
		val ctx = makeTransitionContext(IterableEntity("odd", 1, 0))

		val outcomes = persister.persistWithOutcomes(flowOf(ctx)).toList()

		val indeterminate = outcomes.single() as PersistOutcome.Indeterminate<TestEvt>
		assertThat(indeterminate.error.type).isEqualTo("UNKNOWN_OUTCOME")
		assertThat(indeterminate.error.description).isEqualTo("Weird")
	}

	// ------------------------------------------------------------------
	// Reconciliation
	// ------------------------------------------------------------------

	@Test
	suspend fun `reconciliation promotes a rejected command whose state is already on chain`() {
		val entity = IterableEntity("e-r", 1, 0)
		val intendedPublic = objectMapper.writeValueAsString(entity)
		val perform = scriptedPerform { msgId ->
			CommandOutcome(outcome = "Rejected", msgId = msgId, errorCode = "DUPLICATE_TX", errorMessage = "dup")
		}
		// Target iteration is entity.iteration + 1 = 1; the chain already holds the intended
		// state there — as a decoded map, exercising the non-String canonical comparison.
		val onChainPublic = objectMapper.readValue(intendedPublic, Map::class.java)
		val logs: SsmGetSessionLogsQueryFunction = F2Function { queries ->
			queries.map { query ->
				SsmGetSessionLogsQueryResult(
					ssmName = "test-ssm",
					sessionName = query.sessionName,
					logs = listOf(ssmLog("tx-on-chain", iteration = 1, public = onChainPublic)),
				)
			}
		}
		val persister = buildPersister(perform = perform, logs = logs)

		val outcomes = persister.persistWithOutcomes(flowOf(makeTransitionContext(entity))).toList()

		val success = outcomes.single() as PersistOutcome.Success<TestEvt>
		assertThat(success.msgId).isEqualTo("e-r:1")
		assertThat(success.metadata["transactionId"]).isEqualTo("tx-on-chain")
		assertThat(success.event).isEqualTo(TestEvt(id = "e-r"))
	}

	@Test
	suspend fun `reconciliation promotes when the chain holds the intended state as a string in another key order`() {
		val entity = IterableEntity("e-s", 2, 0)
		val perform = scriptedPerform { msgId ->
			CommandOutcome(outcome = "Rejected", msgId = msgId, errorCode = "DUPLICATE_TX", errorMessage = "dup")
		}
		val reordered = """{"iteration":0,"status":2,"id":"e-s"}"""
		val logs: SsmGetSessionLogsQueryFunction = F2Function { queries ->
			queries.map { query ->
				SsmGetSessionLogsQueryResult(
					ssmName = "test-ssm",
					sessionName = query.sessionName,
					logs = listOf(ssmLog("tx-str", iteration = 1, public = reordered)),
				)
			}
		}
		val persister = buildPersister(perform = perform, logs = logs)

		val outcomes = persister.persistWithOutcomes(flowOf(makeTransitionContext(entity))).toList()

		val success = outcomes.single() as PersistOutcome.Success<TestEvt>
		assertThat(success.metadata["transactionId"]).isEqualTo("tx-str")
	}

	@Test
	suspend fun `reconciliation keeps the base outcome when the chain state differs`() {
		val perform = scriptedPerform { msgId ->
			CommandOutcome(outcome = "Rejected", msgId = msgId, errorCode = "BUSINESS_RULE", errorMessage = "no")
		}
		val logs: SsmGetSessionLogsQueryFunction = F2Function { queries ->
			queries.map { query ->
				SsmGetSessionLogsQueryResult(
					ssmName = "test-ssm",
					sessionName = query.sessionName,
					logs = listOf(ssmLog("tx-other", iteration = 1, public = """{"id":"someone-else"}""")),
				)
			}
		}
		val persister = buildPersister(perform = perform, logs = logs)

		val outcomes = persister.persistWithOutcomes(flowOf(makeTransitionContext(IterableEntity("e-d", 1, 0)))).toList()

		val rejected = outcomes.single() as PersistOutcome.Rejected<TestEvt>
		assertThat(rejected.error.type).isEqualTo("BUSINESS_RULE")
	}

	@Test
	suspend fun `reconciliation falls back to the base outcome when the chain query fails`() {
		val perform = scriptedPerform { msgId ->
			CommandOutcome(outcome = "Rejected", msgId = msgId, errorCode = "DUPLICATE_TX", errorMessage = "dup")
		}
		val failingLogs: SsmGetSessionLogsQueryFunction =
			F2Function { _ -> throw IllegalStateException("chain unreachable") }
		val persister = buildPersister(perform = perform, logs = failingLogs)

		val outcomes = persister.persistWithOutcomes(flowOf(makeTransitionContext(IterableEntity("e-f", 1, 0)))).toList()

		val rejected = outcomes.single() as PersistOutcome.Rejected<TestEvt>
		assertThat(rejected.error.type).isEqualTo("DUPLICATE_TX")
	}

	@Test
	suspend fun `transient outcomes are never reconciled`() {
		val perform = scriptedPerform { msgId ->
			CommandOutcome(outcome = "Transient", msgId = msgId, errorCode = "GRPC_UNAVAILABLE", errorMessage = "down")
		}
		val mustNotQuery: SsmGetSessionLogsQueryFunction =
			F2Function { _ -> error("reconciliation must not query the chain for Transient outcomes") }
		val persister = buildPersister(perform = perform, logs = mustNotQuery)

		val outcomes = persister.persistWithOutcomes(flowOf(makeTransitionContext(IterableEntity("e-t", 1, 0)))).toList()

		val transient = outcomes.single() as PersistOutcome.Transient<TestEvt>
		assertThat(transient.error.type).isEqualTo("GRPC_UNAVAILABLE")
	}

	// ------------------------------------------------------------------
	// withResultAsAction
	// ------------------------------------------------------------------

	@Test
	suspend fun `perform command uses the event class as action when the automate declares results`() {
		val seenActions = mutableListOf<String>()
		val perform: SsmTxSessionPerformActionFunction = F2Function { commands ->
			commands.map { cmd ->
				seenActions += cmd.action
				CommandOutcome(outcome = "Committed", msgId = cmd.msgId, transactionId = "tx", blockNumber = 1L)
			}
		}
		val persister = buildPersister(perform = perform)
		val ctx = makeTransitionContext(IterableEntity("e-a", 1, 0), automate = resultAsActionAutomate)

		persister.persistWithOutcomes(flowOf(ctx)).toList()

		assertThat(seenActions).containsExactly("TestEvt")
	}
}
