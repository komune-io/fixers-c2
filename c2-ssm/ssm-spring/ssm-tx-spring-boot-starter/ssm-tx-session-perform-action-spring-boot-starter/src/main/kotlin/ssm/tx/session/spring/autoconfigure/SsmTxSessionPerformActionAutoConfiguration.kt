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

    // The bean name is the F2 function name consumers resolve: the shortened method keeps it explicitly.
    @Bean("ssmTxSessionPerformActionFunction")
    @ConditionalOnMissingBean(SsmTxSessionPerformActionFunctionImpl::class)
    @ConditionalOnBean(SsmTxService::class)
    fun sessionPerformActionFunction(
        ssmTxService: SsmTxService,
    ): SsmTxSessionPerformActionFunction = SsmTxSessionPerformActionFunctionImpl(ssmTxService)
}
