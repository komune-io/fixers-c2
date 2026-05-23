package ssm.tx.session.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunction
import ssm.chaincode.f2.features.command.SsmTxSessionPerformActionFunctionImpl
import ssm.sdk.core.SsmTxService

@Configuration(proxyBeanMethods = false)
class SsmTxSessionPerformActionAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(name = ["ssmTxSessionPerformActionFunction"])
	@ConditionalOnBean(SsmTxService::class)
	fun ssmTxSessionPerformActionFunction(
		ssmTxService: SsmTxService,
	): SsmTxSessionPerformActionFunction {
		return SsmTxSessionPerformActionFunctionImpl(ssmTxService)
	}
}
