package ssm.tx.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cloud.function.context.FunctionCatalog
import ssm.bdd.config.SsmBddConfig
import ssm.bdd.spring.autoconfigure.ApplicationContextBuilder
import ssm.bdd.spring.autoconfigure.ApplicationContextRunnerBuilder
import ssm.tx.config.spring.autoconfigure.SsmTxProperties
import ssm.tx.create.spring.autoconfigure.SsmTxCreateAutoConfiguration
import ssm.tx.session.start.spring.autoconfigure.SsmSessionStartAutoConfiguration

class ApplicationContextRunnerTest {

    @Test
    fun `spring context runner must must start`() {
        ApplicationContextRunnerBuilder()
            .buildContext(SsmChaincodeConfigTest.localDockerComposeParams)
            .run { context ->
                assertThat(context).hasSingleBean(FunctionCatalog::class.java)
                assertThat(context).hasSingleBean(SsmTxProperties::class.java)
                assertThat(context).hasBean(SsmSessionStartAutoConfiguration::ssmTxSessionStartFunction.name)
                assertThat(context).hasBean(SsmTxCreateAutoConfiguration::ssmTxCreateFunction.name)
                assertThat(context).hasBean("ssmTxSessionPerformActionFunction")
            }
    }


    @Test
    fun `spring context must must start`() {
        val _ = ApplicationContextBuilder().create(
            types = arrayOf(ApplicationContextBuilder.SimpleConfiguration::class.java),
            config = SsmChaincodeConfigTest.localDockerComposeParams
        ).use { context ->
            assertThat(context.getBean(SsmSessionStartAutoConfiguration::ssmTxSessionStartFunction.name)).isNotNull
            assertThat(context.getBean(FunctionCatalog::class.java)).isNotNull
        }
    }

    object SsmChaincodeConfigTest {
        val localDockerCompose = SsmBddConfig.Chaincode

        val localDockerComposeParams = mapOf(
            "ssm.chaincode.url" to localDockerCompose.url,
            "ssm.signer.admin.name" to SsmBddConfig.Key.admin.first,
            "ssm.signer.admin.key" to SsmBddConfig.Key.admin.second,
            "ssm.signer.user.name" to SsmBddConfig.Key.user.first,
            "ssm.signer.user.key" to SsmBddConfig.Key.user.second,
        )
    }

}
