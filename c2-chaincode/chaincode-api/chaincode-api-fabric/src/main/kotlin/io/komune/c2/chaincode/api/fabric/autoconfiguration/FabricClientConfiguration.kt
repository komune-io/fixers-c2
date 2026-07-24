package io.komune.c2.chaincode.api.fabric.autoconfiguration

import io.komune.c2.chaincode.api.config.FabricConfigLoader
import io.komune.c2.chaincode.api.fabric.FabricGatewayBlockClient
import io.komune.c2.chaincode.api.fabric.FabricGatewayBuilder
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(FabricClientProperties::class)
class FabricClientConfiguration  {

    @Bean
    fun fabricGatewayBuilder(fabricConfigLoader: FabricConfigLoader): FabricGatewayBuilder {
        return FabricGatewayBuilder(fabricConfigLoader)
    }

    @Bean
    fun fabricGatewayClient(
        fabricGatewayBuilder: FabricGatewayBuilder,
        properties: FabricClientProperties,
    ): FabricGatewayClient {
        return FabricGatewayClient(
            fabricGatewayBuilder = fabricGatewayBuilder,
            parallelism = properties.parallelism,
        )
    }
    @Bean
    fun fabricGatewayBlockClient(fabricGatewayBuilder: FabricGatewayBuilder): FabricGatewayBlockClient {
        return FabricGatewayBlockClient(
            fabricGatewayBuilder = fabricGatewayBuilder
        )
    }
}
