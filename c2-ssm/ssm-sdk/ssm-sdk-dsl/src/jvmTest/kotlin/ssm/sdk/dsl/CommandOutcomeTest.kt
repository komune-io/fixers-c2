package ssm.sdk.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CommandOutcomeTest {

    private fun outcome(value: String) = CommandOutcome(outcome = value, msgId = "m1")

    @Test
    fun `each wire outcome maps to exactly one predicate`() {
        val byOutcome = listOf("Committed", "Rejected", "Transient", "Conflict", "Indeterminate")
            .associateWith { outcome(it) }

        assertThat(byOutcome.filterValues { it.isSuccess }.keys).containsExactly("Committed")
        assertThat(byOutcome.filterValues { it.isPermanent }.keys).containsExactly("Rejected")
        assertThat(byOutcome.filterValues { it.needsStateCheck }.keys).containsExactly("Indeterminate")
        assertThat(byOutcome.filterValues { it.isRetryable }.keys)
            .containsExactlyInAnyOrder("Transient", "Conflict")
    }

    @Test
    fun `an unknown outcome matches no predicate`() {
        val unknown = outcome("Weird")
        assertThat(unknown.isSuccess).isFalse()
        assertThat(unknown.isRetryable).isFalse()
        assertThat(unknown.isPermanent).isFalse()
        assertThat(unknown.needsStateCheck).isFalse()
    }
}
