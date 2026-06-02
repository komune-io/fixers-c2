package io.komune.c2.ssm.fabric.storing.autoconfiguration

import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.ssm.fabric.storing.FabricSsmClient
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.client.SsmChaincodeClient
import ssm.sdk.sign.SsmCmdSigner

class FabricSsmStoringAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FabricSsmStoringAutoConfiguration::class.java))
        .withUserConfiguration(UpstreamStubs::class.java)

    @Test
    fun `produces SsmChaincodeClient SsmTxService and SsmQueryService`() {
        runner.run { ctx ->
            assertThat(ctx).hasSingleBean(SsmChaincodeClient::class.java)
            assertThat(ctx).hasSingleBean(SsmTxService::class.java)
            assertThat(ctx).hasSingleBean(SsmQueryService::class.java)
            assertThat(ctx.getBean(SsmChaincodeClient::class.java))
                .isInstanceOf(FabricSsmClient::class.java)
        }
    }

    @Test
    fun `kill switch disables wiring when coop_fabric_enabled is false`() {
        runner.withPropertyValues("coop.fabric.enabled=false").run { ctx ->
            assertThat(ctx).doesNotHaveBean(SsmChaincodeClient::class.java)
            assertThat(ctx).doesNotHaveBean(SsmTxService::class.java)
            assertThat(ctx).doesNotHaveBean(SsmQueryService::class.java)
        }
    }

    @Test
    fun `consumer override of SsmChaincodeClient wins via ConditionalOnMissingBean`() {
        val override = mockk<SsmChaincodeClient>(relaxed = true)
        runner.withBean(SsmChaincodeClient::class.java, { override }).run { ctx ->
            assertThat(ctx).hasSingleBean(SsmChaincodeClient::class.java)
            assertThat(ctx.getBean(SsmChaincodeClient::class.java)).isSameAs(override)
        }
    }

    @Configuration
    class UpstreamStubs {
        @Bean fun fabricGatewayClient(): FabricGatewayClient = mockk(relaxed = true)
        @Bean fun ssmCmdSigner(): SsmCmdSigner = mockk(relaxed = true)
        @Bean fun ssmBatchProperties(): SsmBatchProperties = SsmBatchProperties()
    }
}
