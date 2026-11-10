package ssm.tx.config.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmSdkConfig
import ssm.sdk.core.SsmServiceFactory
import ssm.sdk.core.SsmTxService
import ssm.sdk.sign.SsmCmdSigner
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner

@EnableConfigurationProperties(SsmTxProperties::class)
@Configuration(proxyBeanMethods = false)
class SsmTxAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ssm.chaincode", name = ["url"])
    @ConditionalOnMissingBean(SsmChaincodeProperties::class)
    fun chaincodeSsmConfig(ssmTxCreateProperties: SsmTxProperties): SsmChaincodeProperties =
        ssmTxCreateProperties.chaincode!!

    @Bean
    @ConditionalOnMissingBean(SsmBatchProperties::class)
    fun ssmBatchProperties(ssmTxCreateProperties: SsmTxProperties): SsmBatchProperties =
        ssmTxCreateProperties.batch

    @Bean
    @ConditionalOnMissingBean(SsmCmdSignerSha256RSASigner::class)
    fun ssmCmdSigner(ssmTxCreateProperties: SsmTxProperties): SsmCmdSigner {
        return SsmCmdSignerSha256RSASigner(*listOfNotNull(
            ssmTxCreateProperties.signer?.admin?.signer(),
            ssmTxCreateProperties.signer?.user?.signer()
        ).toTypedArray())
    }

    @Bean
    @ConditionalOnBean(value = [SsmCmdSigner::class, SsmChaincodeProperties::class])
    @ConditionalOnMissingBean(SsmTxService::class)
    fun ssmTxService(
        ssmCmdSigner: SsmCmdSigner,
        ssmChaincodeProperties: SsmChaincodeProperties,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmTxService {
        return SsmServiceFactory.builder(
            SsmSdkConfig(ssmChaincodeProperties.url),
            ssmBatchProperties
        ).buildTxService(ssmCmdSigner)
    }

    @Bean
    @ConditionalOnBean(value = [SsmChaincodeProperties::class])
    @ConditionalOnMissingBean(SsmQueryService::class)
    fun ssmQueryService(
        ssmChaincodeProperties: SsmChaincodeProperties,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmQueryService {
        return SsmServiceFactory.builder(
            SsmSdkConfig(ssmChaincodeProperties.url),
            ssmBatchProperties
        ).buildQueryService()
    }
}
