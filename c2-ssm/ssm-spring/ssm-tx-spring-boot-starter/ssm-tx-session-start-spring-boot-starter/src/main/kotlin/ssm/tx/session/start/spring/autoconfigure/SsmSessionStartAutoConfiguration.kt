package ssm.tx.session.start.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.dsl.config.InvokeChunkedProps
import ssm.chaincode.f2.features.command.SsmTxCreateFunctionImpl
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunctionImpl
import ssm.sdk.core.SsmTxService
import ssm.tx.config.spring.autoconfigure.SsmTxProperties
import ssm.tx.dsl.features.ssm.SsmTxSessionStartFunction

@Configuration(proxyBeanMethods = false)
class SsmSessionStartAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(SsmTxCreateFunctionImpl::class)
	@ConditionalOnBean(SsmTxService::class)
	fun ssmTxSessionStartFunction(ssmTxService: SsmTxService, properties: SsmTxProperties): SsmTxSessionStartFunction =
		SsmTxSessionStartFunctionImpl(properties.chunking, ssmTxService)

}
