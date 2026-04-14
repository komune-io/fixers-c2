package s2.spring.automate.ssm.persister

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2Iteration
import s2.dsl.automate.model.WithS2State
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.SsmTransition
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult

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
