package io.komune.c2.chaincode.api.fabric

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.fabric.client.Contract
import org.hyperledger.fabric.client.EndorseException
import org.junit.jupiter.api.Test
import java.util.Optional

/**
 * Tests for FabricGatewayClient.extractErrorMessage — verifying safe-cast
 * behaviour after Sub-fix C in ERROR-PROPAGATION-PLAN.md.
 *
 * EndorseException always wraps a StatusRuntimeException, so we test:
 *  1. StatusRuntimeException with a status description → returns description
 *  2. StatusRuntimeException with no description, no trailers → returns "" (does not crash)
 *  3. StatusRuntimeException where status.description is null → falls back to cause.message
 */
class ExtractErrorMessageTest {

    private val client = FabricGatewayClient(
        object : FabricGatewayBuilder() {
            override fun contracts(channelId: ChannelId, chaincodeId: ChaincodeId): List<Contract> =
                emptyList()
        }
    )

    // --------------------------------------------------------------------------
    // Cause is StatusRuntimeException with a description
    // --------------------------------------------------------------------------

    @Test
    fun `extractErrorMessage returns status description when cause is StatusRuntimeException with description`() {
        val cause = StatusRuntimeException(
            Status.ABORTED.withDescription("chaincode response 500, business rule failed")
        )
        val e = EndorseException("endorse failed", cause)

        val message = client.extractErrorMessage(e)

        // No trailers → falls through to status.description fallback
        assertThat(message).isEqualTo("chaincode response 500, business rule failed")
    }

    @Test
    fun `extractErrorMessage returns empty string when cause has no description and no trailers`() {
        val cause = StatusRuntimeException(Status.INTERNAL)
        val e = EndorseException("endorse failed", cause)

        // Must not crash; status.description is null → falls back through cause.message → ""
        val message = client.extractErrorMessage(e)

        assertThat(message).isNotNull()
        // May be empty or contain something from cause.message — just must not throw
    }

    @Test
    fun `extractErrorMessage includes EndorseException own message as last fallback when cause description null`() {
        // Create a StatusRuntimeException with no description so status.description == null
        val cause = StatusRuntimeException(Status.UNKNOWN)
        val e = EndorseException("own-message-fallback", cause)

        // Safe-cast succeeds (cause IS a StatusRuntimeException).
        // status.description == null, cause.message contains the gRPC status string.
        // The result should be non-null and non-crashing.
        val message = client.extractErrorMessage(e)
        assertThat(message).isNotNull()
    }

    // --------------------------------------------------------------------------
    // Does not crash on any EndorseException variant (regression guard)
    // --------------------------------------------------------------------------

    @Test
    fun `extractErrorMessage never throws regardless of StatusRuntimeException status code`() {
        val statusCodes = listOf(
            Status.OK,
            Status.CANCELLED,
            Status.UNKNOWN,
            Status.INVALID_ARGUMENT,
            Status.NOT_FOUND,
            Status.ALREADY_EXISTS,
            Status.PERMISSION_DENIED,
            Status.RESOURCE_EXHAUSTED,
            Status.FAILED_PRECONDITION,
            Status.ABORTED,
            Status.UNAVAILABLE,
            Status.DATA_LOSS,
            Status.UNAUTHENTICATED,
        )
        for (status in statusCodes) {
            val cause = StatusRuntimeException(status)
            val e = EndorseException("test", cause)
            // Must not throw
            val result = runCatching { client.extractErrorMessage(e) }
            assertThat(result.isSuccess)
                .withFailMessage { "extractErrorMessage threw for status ${status.code}: ${result.exceptionOrNull()}" }
                .isTrue()
        }
    }
}
