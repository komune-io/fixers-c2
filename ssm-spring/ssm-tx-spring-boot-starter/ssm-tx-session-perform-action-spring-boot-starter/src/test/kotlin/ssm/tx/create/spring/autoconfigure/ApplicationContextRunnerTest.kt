package ssm.tx.create.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cloud.function.context.FunctionCatalog
import ssm.bdd.spring.autoconfigure.ApplicationContextBuilder
import ssm.bdd.spring.autoconfigure.ApplicationContextRunnerBuilder
import ssm.bdd.config.SsmBddConfig
import ssm.tx.config.spring.autoconfigure.SsmTxProperties
import ssm.tx.session.spring.autoconfigure.SsmTxSessionPerformActionAutoConfiguration

class ApplicationContextRunnerTest {

	@Test
	fun `spring context runner must must start`() {
		ApplicationContextRunnerBuilder()
			.buildContext(SsmChaincodeConfigTest.localDockerComposeParams).run { context ->
				assertThat(context).hasSingleBean(FunctionCatalog::class.java)
				assertThat(context).hasSingleBean(SsmTxProperties::class.java)
				assertThat(context).hasBean(SsmTxSessionPerformActionAutoConfiguration::ssmTxSessionPerformActionFunction.name)
			}
	}


	@Test
	fun `spring context must must start`() {
		val context = ApplicationContextBuilder().create(
			types = arrayOf(ApplicationContextBuilder.SimpleConfiguration::class.java),
			config = SsmChaincodeConfigTest.localDockerComposeParams
		)
		assertThat(context.getBean(SsmTxSessionPerformActionAutoConfiguration::ssmTxSessionPerformActionFunction.name)).isNotNull
		assertThat(context.getBean(FunctionCatalog::class.java)).isNotNull
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
