package ssm.tx.session.start.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.f2.features.command.SsmTxCreateFunctionImpl
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunctionImpl
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunctionV2
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunctionV2Impl
import ssm.sdk.core.SsmTxService
import ssm.tx.dsl.features.ssm.SsmTxSessionStartFunction

@Configuration(proxyBeanMethods = false)
class SsmSessionStartAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(SsmTxCreateFunctionImpl::class)
	@ConditionalOnBean(SsmTxService::class)
	fun ssmTxSessionStartFunction(
		ssmTxService: SsmTxService,
	): SsmTxSessionStartFunction =
		SsmTxSessionStartFunctionImpl(ssmTxService)

	@Bean
	@ConditionalOnMissingBean(name = ["ssmTxSessionStartFunctionV2"])
	@ConditionalOnBean(SsmTxService::class)
	fun ssmTxSessionStartFunctionV2(
		ssmTxService: SsmTxService,
	): SsmTxSessionStartFunctionV2 =
		SsmTxSessionStartFunctionV2Impl(ssmTxService)

}
