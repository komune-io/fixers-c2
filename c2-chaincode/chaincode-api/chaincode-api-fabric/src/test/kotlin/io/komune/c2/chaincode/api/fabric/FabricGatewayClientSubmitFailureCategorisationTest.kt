package io.komune.c2.chaincode.api.fabric

import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for submit-phase failure categorisation in FabricGatewayClient.
 *
 * Tests the mapSubmitFailure helper which categorises exceptions thrown during
 * the submit phase of a transaction:
 *  - gRPC StatusRuntimeException → TxOutcome.Transient("GRPC_<code>")
 *  - Any other exception          → TxOutcome.Indeterminate("SUBMIT_FAILED")
 *
 * Closes leak Q in tasks/blockchain/ERROR-PROPAGATION.md.
 */
class FabricGatewayClientSubmitFailureCategorisationTest {

    private val client = FabricGatewayClient(
        object : FabricGatewayBuilder() {
            override fun contracts(
                channelId: io.komune.c2.chaincode.dsl.ChannelId,
                chaincodeId: io.komune.c2.chaincode.dsl.ChaincodeId,
            ) = emptyList<org.hyperledger.fabric.client.Contract>()
        }
    )

    @Test
    fun `gRPC UNAVAILABLE during submit becomes Transient with GRPC_UNAVAILABLE code`() {
        val exception = StatusRuntimeException(
            Status.UNAVAILABLE.withDescription("peer not reachable")
        )

        val outcome = client.mapSubmitFailure(exception, commandId = "cmd-42")

        assertThat(outcome).isInstanceOf(TxOutcome.Transient::class.java)
        val transient = outcome as TxOutcome.Transient
        assertThat(transient.commandId).isEqualTo("cmd-42")
        assertThat(transient.errorCode).isEqualTo("GRPC_UNAVAILABLE")
    }

    @Test
    fun `non-gRPC IllegalStateException during submit stays Indeterminate with SUBMIT_FAILED code`() {
        val exception = IllegalStateException("unexpected internal state")

        val outcome = client.mapSubmitFailure(exception, commandId = "cmd-99")

        assertThat(outcome).isInstanceOf(TxOutcome.Indeterminate::class.java)
        val indeterminate = outcome as TxOutcome.Indeterminate
        assertThat(indeterminate.commandId).isEqualTo("cmd-99")
        assertThat(indeterminate.errorCode).isEqualTo("SUBMIT_FAILED")
        assertThat(indeterminate.errorMessage).contains("unexpected internal state")
    }
}
