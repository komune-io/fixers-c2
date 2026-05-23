package ssm.tx.session.start.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunction
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunctionImpl
import ssm.sdk.core.SsmTxService

@Configuration(proxyBeanMethods = false)
class SsmSessionStartAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(name = ["ssmTxSessionStartFunction"])
	@ConditionalOnBean(SsmTxService::class)
	fun ssmTxSessionStartFunction(
		ssmTxService: SsmTxService,
	): SsmTxSessionStartFunction =
		SsmTxSessionStartFunctionImpl(ssmTxService)
}
