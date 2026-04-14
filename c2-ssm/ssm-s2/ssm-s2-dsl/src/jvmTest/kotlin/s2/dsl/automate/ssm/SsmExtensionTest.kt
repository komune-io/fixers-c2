package s2.dsl.automate.ssm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import s2.dsl.automate.S2Automate
import s2.dsl.automate.S2RoleValue
import s2.dsl.automate.S2StateValue
import s2.dsl.automate.S2Transition
import s2.dsl.automate.S2TransitionValue

class SsmExtensionTest {

	private val stateFrom = S2StateValue("Created", 0)
	private val stateTo = S2StateValue("Active", 1)
	private val role = S2RoleValue("Admin")
	private val action = S2TransitionValue("Activate")

	private fun transition(from: S2StateValue? = stateFrom, to: S2StateValue = stateTo) =
		S2Transition(from, to, role, action, null)

	@Test
	fun `toSsmTransition converts transition with non-null from`() {
		val result = transition().toSsmTransition(withResultAsAction = false)
		assertThat(result.from).isEqualTo(0)
		assertThat(result.to).isEqualTo(1)
		assertThat(result.role).isEqualTo("Admin")
		assertThat(result.action).isEqualTo("Activate")
	}

	@Test
	fun `toSsmTransition with from override uses override value`() {
		val result = transition().toSsmTransition(from = 5, withResultAsAction = false)
		assertThat(result.from).isEqualTo(5)
	}

	@Test
	fun `toSsmTransition with to override uses override value`() {
		val result = transition().toSsmTransition(to = 9, withResultAsAction = false)
		assertThat(result.to).isEqualTo(9)
	}

	@Test
	fun `toSsmTransition with null from and no override throws with clear message`() {
		val t = transition(from = null)
		val exception = assertThrows<IllegalArgumentException> {
			t.toSsmTransition(withResultAsAction = false)
		}
		assertThat(exception.message).contains("cannot be null")
	}

	@Test
	fun `toSsmTransition uses result name as action when withResultAsAction is true`() {
		val result = S2TransitionValue("ResultAction")
		val t = S2Transition(stateFrom, stateTo, role, action, result)
		val ssm = t.toSsmTransition(withResultAsAction = true)
		assertThat(ssm.action).isEqualTo("ResultAction")
	}

	@Test
	fun `toSsmTransition uses action name when withResultAsAction is true but result is null`() {
		val t = S2Transition(stateFrom, stateTo, role, action, null)
		val ssm = t.toSsmTransition(withResultAsAction = true)
		assertThat(ssm.action).isEqualTo("Activate")
	}

	@Test
	fun `toSsmTransitions filters out transitions with null from`() {
		val transitions = arrayOf(
			transition(from = stateFrom),
			transition(from = null),
			transition(from = stateFrom),
		)
		val result = transitions.toSsmTransitions(withResultAsAction = false)
		assertThat(result).hasSize(2)
	}

	@Test
	fun `toSsm creates Ssm with correct name and transitions`() {
		val automate = S2Automate(
			name = "TestAutomate",
			version = "1.0",
			transitions = arrayOf(transition()),
		)
		val ssm = automate.toSsm()
		assertThat(ssm.name).isEqualTo("TestAutomate")
		assertThat(ssm.transitions).hasSize(1)
		assertThat(ssm.transitions[0].from).isEqualTo(0)
		assertThat(ssm.transitions[0].to).isEqualTo(1)
	}

	@Test
	fun `toSsm with permissive mode sets all transitions to 0-0`() {
		val automate = S2Automate(
			name = "TestAutomate",
			version = "1.0",
			transitions = arrayOf(transition()),
		)
		val ssm = automate.toSsm(permissive = true)
		assertThat(ssm.transitions).hasSize(1)
		assertThat(ssm.transitions[0].from).isEqualTo(0)
		assertThat(ssm.transitions[0].to).isEqualTo(0)
	}
}
