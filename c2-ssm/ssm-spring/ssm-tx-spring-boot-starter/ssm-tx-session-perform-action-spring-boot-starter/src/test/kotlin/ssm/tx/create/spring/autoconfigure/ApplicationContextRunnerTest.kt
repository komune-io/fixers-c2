package ssm.tx.create.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cloud.function.context.FunctionCatalog
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.bdd.config.SsmBddConfig
import ssm.bdd.spring.autoconfigure.ApplicationContextBuilder
import ssm.bdd.spring.autoconfigure.ApplicationContextRunnerBuilder
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunctionImpl
import ssm.sdk.core.SsmTxService
import ssm.tx.config.spring.autoconfigure.SsmTxProperties

class ApplicationContextRunnerTest {

    @Test
    fun `spring context runner must must start`() {
        ApplicationContextRunnerBuilder()
            .buildContext(SsmChaincodeConfigTest.localDockerComposeParams)
            .run { context ->
                assertThat(context).hasSingleBean(FunctionCatalog::class.java)
                assertThat(context).hasSingleBean(SsmTxProperties::class.java)
                assertThat(context).hasBean("ssmTxSessionPerformActionFunction")
            }
    }


    @Test
    fun `spring context must must start`() {
        val _ = ApplicationContextBuilder().create(
            types = arrayOf(ApplicationContextBuilder.SimpleConfiguration::class.java),
            config = SsmChaincodeConfigTest.localDockerComposeParams
        ).use { context ->
            assertThat(
                context.getBean("ssmTxSessionPerformActionFunction")
            ).isNotNull
            assertThat(context.getBean(FunctionCatalog::class.java)).isNotNull
        }
    }

    @Test
    fun `user-supplied SsmTxSessionPerformActionFunctionImpl bean suppresses the autoconfig default`() {
        ApplicationContextRunnerBuilder()
            .buildContext(SsmChaincodeConfigTest.localDockerComposeParams)
            .withUserConfiguration(CustomPerformConfig::class.java)
            .run { context ->
                assertThat(context).hasBean("customPerformActionFunction")
                assertThat(context).doesNotHaveBean(
                    "ssmTxSessionPerformActionFunction"
                )
                assertThat(context).hasSingleBean(SsmTxSessionPerformActionFunctionImpl::class.java)
            }
    }

    @Configuration
    class CustomPerformConfig {
        @Bean
        fun customPerformActionFunction(ssmTxService: SsmTxService): SsmTxSessionPerformActionFunctionImpl =
            SsmTxSessionPerformActionFunctionImpl(ssmTxService)
    }
}


object SsmChaincodeConfigTest {
    val localDockerCompose = SsmBddConfig.Chaincode

    val localDockerComposeParams = mapOf(
        "ssm.chaincode.url" to localDockerCompose.url,
        "ssm.signer.admin.name" to SsmBddConfig.Key.admin.first,
        "ssm.signer.admin.key" to SsmBddConfig.Key.admin.second,
    )
}
