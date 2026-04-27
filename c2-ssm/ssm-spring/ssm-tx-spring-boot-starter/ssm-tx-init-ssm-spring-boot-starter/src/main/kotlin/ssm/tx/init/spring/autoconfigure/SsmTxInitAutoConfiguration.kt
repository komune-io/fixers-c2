package ssm.tx.init.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.f2.features.command.SsmTxCreateFunctionImpl
import ssm.chaincode.f2.features.command.SsmTxInitFunctionImpl
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.tx.dsl.features.ssm.SsmTxInitFunction

@Configuration(proxyBeanMethods = false)
class SsmTxInitAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(SsmTxCreateFunctionImpl::class)
	@ConditionalOnBean(SsmTxService::class)
	fun ssmTxInitFunction(
		ssmTxService: SsmTxService,
		ssmQueryService: SsmQueryService,
	): SsmTxInitFunction {
		return SsmTxInitFunctionImpl(ssmTxService, ssmQueryService)
	}
}
