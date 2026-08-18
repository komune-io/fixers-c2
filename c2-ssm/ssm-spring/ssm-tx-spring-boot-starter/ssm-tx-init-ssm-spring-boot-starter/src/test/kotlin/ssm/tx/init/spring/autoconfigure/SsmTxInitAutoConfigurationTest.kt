package ssm.tx.init.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.bdd.spring.autoconfigure.ApplicationContextRunnerBuilder
import ssm.chaincode.f2.features.command.SsmTxCreateFunctionImpl
import ssm.chaincode.f2.features.command.SsmTxInitFunctionImpl
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService

/**
 * Pins the `@ConditionalOnMissingBean` semantics of [SsmTxInitAutoConfiguration]:
 * the auto-configured bean must back off for a user-defined [SsmTxInitFunctionImpl]
 * only — not for the unrelated [SsmTxCreateFunctionImpl] (regression test for a
 * copy-paste of the create-starter condition).
 */
class SsmTxInitAutoConfigurationTest {

	private fun contextRunner() = ApplicationContextRunnerBuilder()
		.buildContext(ApplicationContextRunnerTest.SsmChaincodeConfigTest.localDockerComposeParams)

	@Test
	fun `auto-configured init function backs off when the user defines SsmTxInitFunctionImpl`() {
		contextRunner()
			.withUserConfiguration(UserInitFunctionConfiguration::class.java)
			.run { context ->
				assertThat(context).hasSingleBean(SsmTxInitFunctionImpl::class.java)
				assertThat(context).hasBean("userSsmTxInitFunction")
				assertThat(context).doesNotHaveBean(SsmTxInitAutoConfiguration::ssmTxInitFunction.name)
			}
	}

	@Test
	fun `auto-configured init function is kept when the user defines only SsmTxCreateFunctionImpl`() {
		contextRunner()
			.withUserConfiguration(UserCreateFunctionConfiguration::class.java)
			.run { context ->
				assertThat(context).hasBean(SsmTxInitAutoConfiguration::ssmTxInitFunction.name)
				assertThat(context).hasSingleBean(SsmTxInitFunctionImpl::class.java)
				assertThat(context).hasSingleBean(SsmTxCreateFunctionImpl::class.java)
			}
	}

	@Configuration(proxyBeanMethods = false)
	class UserInitFunctionConfiguration {
		@Bean
		fun userSsmTxInitFunction(
			ssmTxService: SsmTxService,
			ssmQueryService: SsmQueryService,
		): SsmTxInitFunctionImpl = SsmTxInitFunctionImpl(ssmTxService, ssmQueryService)
	}

	@Configuration(proxyBeanMethods = false)
	class UserCreateFunctionConfiguration {
		@Bean
		fun userSsmTxCreateFunction(
			ssmTxService: SsmTxService,
		): SsmTxCreateFunctionImpl = SsmTxCreateFunctionImpl(ssmTxService)
	}
}
