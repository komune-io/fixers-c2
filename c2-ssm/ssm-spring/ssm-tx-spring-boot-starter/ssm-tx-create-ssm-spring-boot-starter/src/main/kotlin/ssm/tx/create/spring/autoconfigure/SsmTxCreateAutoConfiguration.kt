package ssm.tx.create.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.f2.features.command.SsmTxCreateFunctionImpl
import ssm.sdk.core.SsmTxService
import ssm.tx.config.spring.autoconfigure.SsmTxProperties
import ssm.tx.dsl.features.ssm.SsmTxCreateFunction

@Configuration(proxyBeanMethods = false)
class SsmTxCreateAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(SsmTxCreateFunctionImpl::class)
	@ConditionalOnBean(SsmTxService::class)
	fun ssmTxCreateFunction(
		ssmTxService: SsmTxService,
	): SsmTxCreateFunction {
		return SsmTxCreateFunctionImpl(ssmTxService)
	}
}
