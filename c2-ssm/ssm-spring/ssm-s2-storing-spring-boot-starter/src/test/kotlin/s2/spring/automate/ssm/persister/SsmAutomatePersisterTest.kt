package s2.spring.automate.ssm.persister

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2Iteration
import s2.dsl.automate.model.WithS2State

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
		// Before the fix, the code was: entityType.isAssignableFrom(WithS2Iteration::class.java)
		// This asks "is SimpleEntity a superclass of WithS2Iteration?" — always false for concrete entities
		val invertedResult = SimpleEntity::class.java.isAssignableFrom(WithS2Iteration::class.java)
		assertThat(invertedResult).isFalse()

		// Even for IterableEntity, the inverted check is wrong
		val invertedResult2 = IterableEntity::class.java.isAssignableFrom(WithS2Iteration::class.java)
		assertThat(invertedResult2).isFalse()

		// The correct check:
		val correctResult = WithS2Iteration::class.java.isAssignableFrom(IterableEntity::class.java)
		assertThat(correctResult).isTrue()
	}
}
