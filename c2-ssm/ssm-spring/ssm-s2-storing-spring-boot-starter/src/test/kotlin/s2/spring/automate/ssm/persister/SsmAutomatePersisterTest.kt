package s2.spring.automate.ssm.persister

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.TransitionAppliedContext
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2Command
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
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.sdk.dsl.CommandOutcome
import tools.jackson.databind.ObjectMapper

class SsmAutomatePersisterTest {

	interface TestState : s2.dsl.automate.S2State {
		override val position: Int
	}

	data class SimpleEntity(
		val id: String,
		val status: Int
	) : WithS2Id<String>, WithS2State<TestState> {
		override fun s2Id() = id
		override fun s2State() = object : TestState {
			override val position = status
		}
	}

	data class IterableEntity(
		val id: String,
		val status: Int,
		val iteration: Int
	) : WithS2Id<String>, WithS2State<TestState>, WithS2Iteration {
		override fun s2Id() = id
		override fun s2State() = object : TestState {
			override val position = status
		}
		override fun s2Iteration() = iteration
	}

	@Test
	fun `isAssignableFrom correctly identifies entity implementing WithS2Iteration`() {
		val result = WithS2Iteration::class.java.isAssignableFrom(IterableEntity::class.java)
		assertThat(result).isTrue()
	}

	@Test
	fun `isAssignableFrom correctly rejects entity not implementing WithS2Iteration`() {
		val result = WithS2Iteration::class.java.isAssignableFrom(SimpleEntity::class.java)
		assertThat(result).isFalse()
	}

	@Test
	fun `inverted isAssignableFrom gives wrong result - this is what the bug was`() {
		val invertedResult = SimpleEntity::class.java.isAssignableFrom(WithS2Iteration::class.java)
		assertThat(invertedResult).isFalse()

		val invertedResult2 = IterableEntity::class.java.isAssignableFrom(WithS2Iteration::class.java)
		assertThat(invertedResult2).isFalse()

		val correctResult = WithS2Iteration::class.java.isAssignableFrom(IterableEntity::class.java)
		assertThat(correctResult).isTrue()
	}

	@Test
	fun `empty session logs should be detected as error condition`() {
		val session = SsmGetSessionLogsQueryResult(
			ssmName = "test-ssm",
			sessionName = "session-1",
			logs = emptyList()
		)
		val maxIteration = session.logs.maxOfOrNull { it.state.iteration }
		assertThat(maxIteration).isNull()
	}

	@Test
	fun `non-empty session logs return max iteration`() {
		val session = SsmGetSessionLogsQueryResult(
			ssmName = "test-ssm",
			sessionName = "session-1",
			logs = listOf(
				ssmLog("tx-0", 0),
				ssmLog("tx-1", 1),
				ssmLog("tx-2", 2),
			)
		)
		val maxIteration = session.logs.maxOfOrNull { it.state.iteration }
		assertThat(maxIteration).isEqualTo(2)
	}

	@Test
	fun `associateBy on session map drops duplicates silently`() {
		data class Entry(val sessionId: String, val value: String)

		val entries = listOf(
			Entry("session-1", "first"),
			Entry("session-1", "second"),
			Entry("session-2", "third"),
		)
		val bySession = entries.associateBy { it.sessionId }
		// associateBy keeps only the LAST entry for duplicates
		assertThat(bySession).hasSize(2)
		assertThat(bySession["session-1"]?.value).isEqualTo("second")
		// This proves why the code must guard against duplicate sessions in a batch
	}

	@Test
	fun `missing session in map should be detected`() {
		val bySession = mapOf("session-1" to "context-1")
		val missingKey = "session-unknown"

		assertThat(bySession[missingKey]).isNull()

		val exception = assertThrows<IllegalStateException> {
			bySession[missingKey]
				?: throw IllegalStateException("No context found for session $missingKey")
		}
		assertThat(exception.message).contains("session-unknown")
	}

	// --- Fixtures for persistWithOutcomes test ---

	/** Marker event type used in the v2 persist test. */
	data class TestEvt(val id: String = "") : Event

	/** Command type used as the `msg` in TransitionAppliedContext. */
	data class TestCommand(override val id: String) : S2Command<String>

	/**
	 * Minimal S2Automate with one transition (result = null so withResultAsAction = false,
	 * meaning the persister uses msg::class.simpleName as the action name).
	 */
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

	private fun makeTransitionContext(
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

	private fun makeSimpleTransitionContext(
		entity: SimpleEntity,
	): TransitionAppliedContext<TestState, String, SimpleEntity, TestEvt, S2Automate> {
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

	@Test
	fun `persistWithOutcomes maps mixed v2 results to per-item PersistOutcome`() = runTest {
		// Stub v2 perform: items whose commandId contains "id-3" return Rejected, others Committed.
		// commandId pattern is "${sessionId}:${iteration + 1}" per SsmAutomatePersister source.
		val v2Perform: ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction =
			F2Function { commands ->
				commands.map { cmd ->
					if (cmd.msgId.contains("id-3")) {
						CommandOutcome(
							outcome = "Rejected",
							msgId = cmd.msgId,
							errorCode = "MVCC_READ_CONFLICT",
							errorMessage = "stale read",
						)
					} else {
						CommandOutcome(
							outcome = "Committed",
							msgId = cmd.msgId,
							transactionId = "tx-${cmd.msgId}",
							blockNumber = 100L,
						)
					}
				}
			}

		val v2Start: ssm.chaincode.f2.features.command.SsmTxSessionStartFunction =
			F2Function { _ -> error("start should not be called by persistWithOutcomes") }

		val noLogs: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { _ -> error("ssmGetSessionLogsQueryFunction should not be called for IterableEntity") }

		val persister = SsmAutomatePersister<TestState, String, IterableEntity, TestEvt>(
			ssmSessionStartFunction = v2Start,
			ssmSessionPerformActionFunction = v2Perform,
			ssmGetSessionLogsQueryFunction = noLogs,
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			entityType = IterableEntity::class.java,
			agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
			objectMapper = ObjectMapper(),
			batch = S2BatchProperties(),
		)

		val ctx1 = makeTransitionContext(IterableEntity("id-1", status = 1, iteration = 0))
		val ctx2 = makeTransitionContext(IterableEntity("id-2", status = 1, iteration = 0))
		val ctx3 = makeTransitionContext(IterableEntity("id-3", status = 1, iteration = 0))

		val outcomes = persister.persistWithOutcomes(flowOf(ctx1, ctx2, ctx3)).toList()

		assertThat(outcomes).hasSize(3)
		assertThat(outcomes.filterIsInstance<PersistOutcome.Success<TestEvt>>()).hasSize(2)
		val rejected = outcomes.filterIsInstance<PersistOutcome.Rejected<TestEvt>>().single()
		assertThat(rejected.error.type).isEqualTo("MVCC_READ_CONFLICT")
		assertThat(rejected.msgId).contains("id-3")
	}

	@Test
	fun `persistWithOutcomes emits Rejected for sessions missing from chaincode (lookup branch)`() = runTest {
		val performCommandsSeen = mutableListOf<String>()

		// v2 perform: capture which commands were sent, return Committed for each
		val v2Perform: ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction =
			F2Function { commands ->
				commands.map { cmd ->
					performCommandsSeen.add(cmd.msgId)
					CommandOutcome(
						outcome = "Committed",
						msgId = cmd.msgId,
						transactionId = "tx-${cmd.msgId}",
						blockNumber = 1L,
					)
				}
			}

		// Session-logs query: return sess-1 and sess-3 only (omit sess-2)
		val logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
			F2Function { queries ->
				queries.toList()
					.filter { it.sessionName != "sess-2" }
					.map { q ->
						SsmGetSessionLogsQueryResult(
							ssmName = "test-ssm",
							sessionName = q.sessionName,
							logs = listOf(ssmLog("tx-${q.sessionName}", iteration = 0)),
						)
					}
					.asFlow()
			}

		val v2Start: ssm.chaincode.f2.features.command.SsmTxSessionStartFunction =
			F2Function { _ -> error("start should not be called") }

		val persister = SsmAutomatePersister<TestState, String, SimpleEntity, TestEvt>(
			ssmSessionStartFunction = v2Start,
			ssmSessionPerformActionFunction = v2Perform,
			ssmGetSessionLogsQueryFunction = logsQuery,
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			entityType = SimpleEntity::class.java,
			agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
			objectMapper = ObjectMapper(),
			batch = S2BatchProperties(),
		)

		val ctx1 = makeSimpleTransitionContext(SimpleEntity("sess-1", status = 1))
		val ctx2 = makeSimpleTransitionContext(SimpleEntity("sess-2", status = 1))
		val ctx3 = makeSimpleTransitionContext(SimpleEntity("sess-3", status = 1))

		val outcomes = persister.persistWithOutcomes(flowOf(ctx1, ctx2, ctx3)).toList()

		assertThat(outcomes).hasSize(3)

		val rejected = outcomes.filterIsInstance<PersistOutcome.Rejected<TestEvt>>().single()
		assertThat(rejected.error.type).isEqualTo("SESSION_NOT_FOUND")
		assertThat(rejected.msgId).contains("sess-2")

		val committed = outcomes.filterIsInstance<PersistOutcome.Success<TestEvt>>()
		assertThat(committed).hasSize(2)
		assertThat(committed.map { it.msgId }).noneMatch { it.contains("sess-2") }

		// Critical: the missing-session item must NOT have reached the chaincode call
		assertThat(performCommandsSeen).hasSize(2)
		assertThat(performCommandsSeen).noneMatch { it.contains("sess-2") }
	}

	// --- end fixtures ---

	private fun ssmLog(txId: String, iteration: Int): SsmSessionStateLog {
		return SsmSessionStateLog(
			txId = txId,
			state = SsmSessionState(
				ssm = "test-ssm",
				session = "session-1",
				roles = mapOf("admin" to "Admin"),
				public = "{}",
				private = emptyMap(),
				origin = SsmTransition(from = 0, to = 1, role = "Admin", action = "Create"),
				current = 1,
				iteration = iteration,
			)
		)
	}
}
