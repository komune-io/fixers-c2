package ssm.tx.create.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cloud.function.context.FunctionCatalog
import ssm.bdd.config.SsmBddConfig
import ssm.bdd.spring.autoconfigure.ApplicationContextBuilder
import ssm.bdd.spring.autoconfigure.ApplicationContextRunnerBuilder
import ssm.tx.config.spring.autoconfigure.SsmTxProperties

class ApplicationContextRunnerTest {

    @Test
    fun `spring context runner must must start`() {
        ApplicationContextRunnerBuilder()
            .buildContext(SsmChaincodeConfigTest.localDockerComposeParams)
            .run { context ->
                assertThat(context).hasSingleBean(FunctionCatalog::class.java)
                assertThat(context).hasSingleBean(SsmTxProperties::class.java)
                assertThat(context).hasBean(SsmTxCreateAutoConfiguration::ssmTxCreateFunction.name)
            }
    }


    @Test
    fun `spring context must must start`() {
        val _ = ApplicationContextBuilder().create(
            types = arrayOf(ApplicationContextBuilder.SimpleConfiguration::class.java),
            config = SsmChaincodeConfigTest.localDockerComposeParams
        ).use { context ->
            assertThat(context.getBean(SsmTxCreateAutoConfiguration::ssmTxCreateFunction.name)).isNotNull
            assertThat(context.getBean(FunctionCatalog::class.java)).isNotNull
        }
    }


    object SsmChaincodeConfigTest {
        val localDockerComposeParams = mapOf(
            "ssm.chaincode.url" to SsmBddConfig.Chaincode.config.url,
            "ssm.signer.admin.name" to SsmBddConfig.Key.admin.first,
            "ssm.signer.admin.key" to SsmBddConfig.Key.admin.second,
        )
    }
}
