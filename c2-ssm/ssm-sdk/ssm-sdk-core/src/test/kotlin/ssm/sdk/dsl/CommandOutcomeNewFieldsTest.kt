package ssm.sdk.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Verifies that CommandOutcome carries the new errorClass + errorOrigin fields
 * with "UNKNOWN" defaults for backward compatibility.
 *
 * Task 3.3 — propagate errorClass + errorOrigin through CommandOutcome chain.
 */
class CommandOutcomeNewFieldsTest {

    @Test
    fun `CommandOutcome defaults errorClass and errorOrigin to UNKNOWN`() {
        val outcome = CommandOutcome(
            outcome = "Rejected",
            commandId = "cmd-1",
            errorCode = "SOME_ERROR",
            errorMessage = "something failed",
        )
        assertThat(outcome.errorClass).isEqualTo("UNKNOWN")
        assertThat(outcome.errorOrigin).isEqualTo("UNKNOWN")
    }

    @Test
    fun `CommandOutcome preserves explicit errorClass and errorOrigin`() {
        val outcome = CommandOutcome(
            outcome = "Rejected",
            commandId = "cmd-2",
            errorCode = "SIGN_FAILED",
            errorMessage = "key not found",
            errorClass = "AUTH",
            errorOrigin = "C2_SDK",
        )
        assertThat(outcome.errorClass).isEqualTo("AUTH")
        assertThat(outcome.errorOrigin).isEqualTo("C2_SDK")
    }

    @Test
    fun `CommandOutcome success variant has UNKNOWN class and origin by default`() {
        val outcome = CommandOutcome(
            outcome = "Committed",
            commandId = "cmd-3",
            transactionId = "tx-abc",
            blockNumber = 42L,
        )
        assertThat(outcome.errorClass).isEqualTo("UNKNOWN")
        assertThat(outcome.errorOrigin).isEqualTo("UNKNOWN")
    }

    @Test
    fun `isSuccess isRetryable isPermanent predicates still work`() {
        val committed = CommandOutcome(outcome = "Committed", commandId = "c")
        val transient = CommandOutcome(outcome = "Transient", commandId = "t")
        val rejected = CommandOutcome(outcome = "Rejected", commandId = "r")
        val conflict = CommandOutcome(outcome = "Conflict", commandId = "cf")
        val indeterminate = CommandOutcome(outcome = "Indeterminate", commandId = "i")

        assertThat(committed.isSuccess).isTrue()
        assertThat(transient.isRetryable).isTrue()
        assertThat(conflict.isRetryable).isTrue()
        assertThat(rejected.isPermanent).isTrue()
        assertThat(indeterminate.needsStateCheck).isTrue()
    }
}
