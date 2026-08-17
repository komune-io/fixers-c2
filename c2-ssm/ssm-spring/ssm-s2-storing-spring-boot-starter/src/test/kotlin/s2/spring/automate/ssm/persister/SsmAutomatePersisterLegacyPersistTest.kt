package s2.spring.automate.ssm.persister

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.InitTransitionAppliedContext
import s2.automate.core.context.TransitionAppliedContext
import s2.automate.core.error.AutomateException
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
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.sdk.dsl.CommandOutcome
import tools.jackson.databind.ObjectMapper

/**
 * Pins the legacy [SsmAutomatePersister.persist] / [SsmAutomatePersister.persistInit]
 * collapse of the outcomes pipeline.
 *
 * The s2 legacy engine correlates events to contexts positionally and expects exactly
 * one event per received context, in order. A failure must therefore surface as a
 * thrown [AutomateException] — never as a silent under-emit (the previous `mapNotNull`
 * behavior), which shifted every later event one position left and misrouted the
 * engine's end-of-transition application events.
 */
class SsmAutomatePersisterLegacyPersistTest {

	interface TestState : s2.dsl.automate.S2State {
		override val position: Int
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

	data class SimpleEntity(
		val id: String,
		val status: Int,
	) : WithS2Id<String>, WithS2State<TestState> {
		override fun s2Id() = id
		override fun s2State() = object : TestState { override val position = status }
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

	private fun <ENTITY> makeContext(
		entity: ENTITY,
	): TransitionAppliedContext<TestState, String, ENTITY, TestEvt, S2Automate> where
	ENTITY : WithS2Id<String>,
	ENTITY : WithS2State<TestState> {
		return TransitionAppliedContext(
			automateContext = AutomateContext(automate = testAutomate, batch = S2BatchProperties()),
			msgId = entity.s2Id(),
			from = entity.s2State(),
			msg = TestCommand(id = entity.s2Id()),
			event = TestEvt(id = entity.s2Id()),
			entity = entity,
		)
	}

	private fun makeInitContext(
		entity: IterableEntity,
	): InitTransitionAppliedContext<TestState, String, IterableEntity, TestEvt, S2Automate> =
		InitTransitionAppliedContext(
			automateContext = AutomateContext(automate = testAutomate, batch = S2BatchProperties()),
			msgId = "start:${entity.id}",
			msg = TestInitCommand(entityId = entity.id),
			event = TestEvt(id = entity.id),
			entity = entity,
		)

	/** Perform stub: rejects commands whose msgId contains [rejectedIdFragment], commits the rest. */
	private fun performFunction(
		rejectedIdFragment: String? = null,
	): ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction = F2Function { commands ->
		commands.map { cmd ->
			if (rejectedIdFragment != null && cmd.msgId.contains(rejectedIdFragment)) {
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
					blockNumber = 1L,
				)
			}
		}
	}

	private fun startFunction(
		rejectedIdFragment: String? = null,
	): ssm.chaincode.f2.features.command.SsmTxSessionStartFunction = F2Function { commands ->
		commands.map { cmd ->
			if (rejectedIdFragment != null && cmd.msgId.contains(rejectedIdFragment)) {
				CommandOutcome(
					outcome = "Rejected",
					msgId = cmd.msgId,
					errorCode = "SESSION_ALREADY_EXISTS",
					errorMessage = "session exists",
				)
			} else {
				CommandOutcome(
					outcome = "Committed",
					msgId = cmd.msgId,
					transactionId = "tx-${cmd.msgId}",
					blockNumber = 1L,
				)
			}
		}
	}

	private val noSessionsOnChain: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction =
		F2Function { _ -> flowOf<SsmGetSessionLogsQueryResult>() }

	private fun <ENTITY> persister(
		entityType: Class<ENTITY>,
		perform: ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction = performFunction(),
		start: ssm.chaincode.f2.features.command.SsmTxSessionStartFunction = startFunction(),
		logsQuery: ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction = noSessionsOnChain,
	): SsmAutomatePersister<TestState, String, ENTITY, TestEvt> where
	ENTITY : WithS2Id<String>,
	ENTITY : WithS2State<TestState> {
		return SsmAutomatePersister(
			ssmSessionStartFunction = start,
			ssmSessionPerformActionFunction = perform,
			ssmGetSessionLogsQueryFunction = logsQuery,
			chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
			entityType = entityType,
			agentSigner = Agent(name = "test-agent", pub = ByteArray(0)),
			objectMapper = ObjectMapper(),
			batch = S2BatchProperties(),
		)
	}

	// -------------------------------------------------------------------------
	// persist (perform path)
	// -------------------------------------------------------------------------

	@Test
	suspend fun `persist happy batch emits one event per context in order`() {
		val persister = persister(IterableEntity::class.java)
		val contexts = listOf("id-1", "id-2", "id-3").map {
			makeContext(IterableEntity(it, status = 1, iteration = 0))
		}

		val events = persister.persist(contexts.asFlow()).toList()

		assertThat(events).containsExactly(TestEvt("id-1"), TestEvt("id-2"), TestEvt("id-3"))
	}

	@Test
	suspend fun `persist mixed batch throws AutomateException in position, prefix events stay correlated`() {
		val persister = persister(IterableEntity::class.java, perform = performFunction(rejectedIdFragment = "id-3"))
		val contexts = listOf("id-1", "id-2", "id-3", "id-4").map {
			makeContext(IterableEntity(it, status = 1, iteration = 0))
		}

		val received = mutableListOf<TestEvt>()
		val exception = assertThrows<AutomateException> {
			persister.persist(contexts.asFlow()).toList(received)
		}

		// Events before the failing item are emitted in position; nothing after it leaks out.
		assertThat(received).containsExactly(TestEvt("id-1"), TestEvt("id-2"))
		assertThat(exception.errors.single().type).isEqualTo("MVCC_READ_CONFLICT")
	}

	@Test
	suspend fun `persist mixed batch with missing session throws before emitting any event`() {
		val performCommandsSeen = mutableListOf<String>()
		val countingPerform: ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction =
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
		// Chain knows sess-1 and sess-3 only — sess-2's lookup is a Rejected outcome.
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
		val persister = persister(SimpleEntity::class.java, perform = countingPerform, logsQuery = logsQuery)
		val contexts = listOf("sess-1", "sess-2", "sess-3").map { makeContext(SimpleEntity(it, status = 1)) }

		val received = mutableListOf<TestEvt>()
		val exception = assertThrows<AutomateException> {
			persister.persist(contexts.asFlow()).toList(received)
		}

		// Lookup failures are emitted before any chaincode command: deterministic error,
		// zero events emitted, zero commands sent — nothing is misrouted.
		assertThat(received).isEmpty()
		assertThat(exception.errors.single().type).isEqualTo("SESSION_NOT_FOUND")
		assertThat(performCommandsSeen).isEmpty()
	}

	// -------------------------------------------------------------------------
	// persistInit (start path)
	// -------------------------------------------------------------------------

	@Test
	suspend fun `persistInit happy batch emits one event per context in order`() {
		val persister = persister(IterableEntity::class.java)
		val contexts = listOf("id-1", "id-2").map {
			makeInitContext(IterableEntity(it, status = 1, iteration = 0))
		}

		val events = persister.persistInit(contexts.asFlow()).toList()

		assertThat(events).containsExactly(TestEvt("id-1"), TestEvt("id-2"))
	}

	@Test
	suspend fun `persistInit mixed batch throws AutomateException instead of dropping the failure`() {
		val persister = persister(IterableEntity::class.java, start = startFunction(rejectedIdFragment = "id-2"))
		val contexts = listOf("id-1", "id-2", "id-3").map {
			makeInitContext(IterableEntity(it, status = 1, iteration = 0))
		}

		val received = mutableListOf<TestEvt>()
		val exception = assertThrows<AutomateException> {
			persister.persistInit(contexts.asFlow()).toList(received)
		}

		assertThat(received).containsExactly(TestEvt("id-1"))
		assertThat(exception.errors.single().type).isEqualTo("SESSION_ALREADY_EXISTS")
	}

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
