package io.komune.c2.chaincode.api.fabric.autoconfiguration

import io.komune.c2.chaincode.api.fabric.FabricGatewayBuilder
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.fabric.client.Contract
import org.junit.jupiter.api.Test

/**
 * Wiring tests for FabricClientConfiguration bean factory methods — no Spring context
 * required, the methods are called directly with a stub builder.
 */
class FabricClientConfigurationTest {

    private val builder = object : FabricGatewayBuilder() {
        override fun contracts(channelId: ChannelId, chaincodeId: ChaincodeId): List<Contract> = emptyList()
    }

    @Test
    fun `builds the gateway client from the bound properties`() {
        val client = FabricClientConfiguration().fabricGatewayClient(
            fabricGatewayBuilder = builder,
            properties = FabricClientProperties().apply { parallelism = 2 },
        )
        assertThat(client.parallelIO).isNotNull
    }

    @Test
    fun `builds the block client`() {
        assertThat(FabricClientConfiguration().fabricGatewayBlockClient(builder)).isNotNull
    }
}
