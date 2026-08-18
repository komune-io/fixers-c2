package s2.spring.automate.ssm.persister

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.automate.core.context.InitTransitionAppliedContext
import s2.automate.core.context.TransitionAppliedContext
import s2.automate.core.error.AutomateException
import s2.automate.core.persist.PersistOutcome
import s2.dsl.automate.ErrorCategory
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2ErrorBase
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
 * collapse of the outcomes pipeline: exactly one event per context in order, a failure
 * surfacing as a thrown [AutomateException] rather than a silent under-emit (which the
 * legacy engine, correlating events to contexts positionally, cannot detect in time).
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

	private fun performFunction(
		rejectedIdFragment: String? = null,
		failureOutcome: String = "Rejected",
		failureCode: String = "MVCC_READ_CONFLICT",
		failureMessage: String = "stale read",
	): ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction = F2Function { commands ->
		commands.map { cmd ->
			if (rejectedIdFragment != null && cmd.msgId.contains(rejectedIdFragment)) {
				CommandOutcome(
					outcome = failureOutcome,
					msgId = cmd.msgId,
					errorCode = failureCode,
					errorMessage = failureMessage,
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
		failureOutcome: String = "Rejected",
		failureCode: String = "SESSION_ALREADY_EXISTS",
		failureMessage: String = "session exists",
	): ssm.chaincode.f2.features.command.SsmTxSessionStartFunction = F2Function { commands ->
		commands.map { cmd ->
			if (rejectedIdFragment != null && cmd.msgId.contains(rejectedIdFragment)) {
				CommandOutcome(
					outcome = failureOutcome,
					msgId = cmd.msgId,
					errorCode = failureCode,
					errorMessage = failureMessage,
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

		assertThat(received).containsExactly(TestEvt("id-1"), TestEvt("id-2"))
		assertThat(exception.errors.single().type).isEqualTo("MVCC_READ_CONFLICT")
		assertThat(exception.errors.single().payload).containsEntry("category", "Rejected")
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

		assertThat(received).isEmpty()
		assertThat(exception.errors.single().type).isEqualTo("SESSION_NOT_FOUND")
		assertThat(exception.errors.single().payload).containsEntry("category", "Rejected")
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
		assertThat(exception.errors.single().payload).containsEntry("category", "Rejected")
	}

	// -------------------------------------------------------------------------
	// ErrorCategory survives the legacy collapse
	//
	// PersistOutcome.Failure carries both an S2Error and an ErrorCategory, but the
	// legacy paths can only throw. Dropping the category would make a retryable
	// Transient indistinguishable from an Indeterminate (command may already have
	// committed — a blind retry risks a duplicate on-chain transition) and from a
	// permanent Rejected. It is therefore carried in the thrown error's payload.
	// -------------------------------------------------------------------------

	@Test
	suspend fun `persist Transient failure stays distinguishable via the category payload`() {
		val persister = persister(
			IterableEntity::class.java,
			perform = performFunction(
				rejectedIdFragment = "id-2",
				failureOutcome = "Transient",
				failureCode = "GRPC_UNAVAILABLE",
				failureMessage = "peer unavailable",
			),
		)
		val contexts = listOf("id-1", "id-2").map { makeContext(IterableEntity(it, status = 1, iteration = 0)) }

		val received = mutableListOf<TestEvt>()
		val exception = assertThrows<AutomateException> {
			persister.persist(contexts.asFlow()).toList(received)
		}

		assertThat(received).containsExactly(TestEvt("id-1"))
		val error = exception.errors.single()
		assertThat(error.type).isEqualTo("GRPC_UNAVAILABLE")
		assertThat(error.description).isEqualTo("peer unavailable")
		assertThat(error.payload).containsEntry("category", ErrorCategory.Transient.name)
	}

	@Test
	suspend fun `persist Indeterminate failure stays distinguishable via the category payload`() {
		val persister = persister(
			IterableEntity::class.java,
			perform = performFunction(
				rejectedIdFragment = "id-2",
				failureOutcome = "Indeterminate",
				failureCode = "COMMIT_TIMEOUT",
				failureMessage = "no commit event received",
			),
		)
		val contexts = listOf("id-1", "id-2").map { makeContext(IterableEntity(it, status = 1, iteration = 0)) }

		val received = mutableListOf<TestEvt>()
		val exception = assertThrows<AutomateException> {
			persister.persist(contexts.asFlow()).toList(received)
		}

		assertThat(received).containsExactly(TestEvt("id-1"))
		val error = exception.errors.single()
		assertThat(error.type).isEqualTo("COMMIT_TIMEOUT")
		assertThat(error.description).isEqualTo("no commit event received")
		assertThat(error.payload).containsEntry("category", ErrorCategory.Indeterminate.name)
	}

	@Test
	suspend fun `persistInit Transient failure stays distinguishable via the category payload`() {
		val persister = persister(
			IterableEntity::class.java,
			start = startFunction(
				rejectedIdFragment = "id-2",
				failureOutcome = "Transient",
				failureCode = "GRPC_UNAVAILABLE",
				failureMessage = "peer unavailable",
			),
		)
		val contexts = listOf("id-1", "id-2").map { makeInitContext(IterableEntity(it, status = 1, iteration = 0)) }

		val exception = assertThrows<AutomateException> {
			persister.persistInit(contexts.asFlow()).toList()
		}

		val error = exception.errors.single()
		assertThat(error.type).isEqualTo("GRPC_UNAVAILABLE")
		assertThat(error.payload).containsEntry("category", ErrorCategory.Transient.name)
	}

	@Test
	suspend fun `persistInit Indeterminate failure stays distinguishable via the category payload`() {
		val persister = persister(
			IterableEntity::class.java,
			start = startFunction(
				rejectedIdFragment = "id-2",
				failureOutcome = "Indeterminate",
				failureCode = "COMMIT_TIMEOUT",
				failureMessage = "no commit event received",
			),
		)
		val contexts = listOf("id-1", "id-2").map { makeInitContext(IterableEntity(it, status = 1, iteration = 0)) }

		val exception = assertThrows<AutomateException> {
			persister.persistInit(contexts.asFlow()).toList()
		}

		val error = exception.errors.single()
		assertThat(error.type).isEqualTo("COMMIT_TIMEOUT")
		assertThat(error.payload).containsEntry("category", ErrorCategory.Indeterminate.name)
	}

	/**
	 * The persister never builds a failure with a non-empty payload, so only a direct call can
	 * prove the category is *merged into* the original payload rather than replacing it. Also pins
	 * that type, description, date and cause are copied through untouched.
	 */
	@Test
	fun `asCategorizedException merges the category into the original payload`() {
		val cause = IllegalStateException("boom")
		val outcome = PersistOutcome.Indeterminate<TestEvt>(
			msgId = "msg-1",
			error = S2ErrorBase(
				type = "COMMIT_TIMEOUT",
				description = "no commit event received",
				date = "2026-01-01",
				payload = mapOf("sessionName" to "sess-1", "txId" to "tx-1"),
				cause = cause,
			),
		)

		val error = outcome.asCategorizedException().errors.single()

		assertThat(error.type).isEqualTo("COMMIT_TIMEOUT")
		assertThat(error.description).isEqualTo("no commit event received")
		assertThat(error.date).isEqualTo("2026-01-01")
		assertThat(error.cause).isSameAs(cause)
		assertThat(error.payload).containsOnly(
			entry("sessionName", "sess-1"),
			entry("txId", "tx-1"),
			entry("category", ErrorCategory.Indeterminate.name),
		)
	}

	/** All four categories map to a distinct payload value — the whole point of carrying it. */
	@Test
	fun `asCategorizedException carries every category distinctly`() {
		val error = S2ErrorBase(type = "E", description = "d", date = "", payload = emptyMap())
		val categoryOf = { outcome: PersistOutcome.Failure<TestEvt> ->
			outcome.asCategorizedException().errors.single().payload["category"]
		}

		assertThat(categoryOf(PersistOutcome.Rejected("m", error))).isEqualTo("Rejected")
		assertThat(categoryOf(PersistOutcome.Transient("m", error))).isEqualTo("Transient")
		assertThat(categoryOf(PersistOutcome.Indeterminate("m", error))).isEqualTo("Indeterminate")
		assertThat(categoryOf(PersistOutcome.Conflict("m", error))).isEqualTo("Conflict")
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
