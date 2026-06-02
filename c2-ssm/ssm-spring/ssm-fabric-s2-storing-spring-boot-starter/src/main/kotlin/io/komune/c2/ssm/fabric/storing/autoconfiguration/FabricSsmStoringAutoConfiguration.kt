package io.komune.c2.ssm.fabric.storing.autoconfiguration

import io.komune.c2.chaincode.api.config.autoconfiguration.C2ChaincodeAutoConfiguration
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.autoconfiguration.FabricClientConfiguration
import io.komune.c2.ssm.fabric.storing.FabricSsmClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.f2.query.SsmGetSessionLogsQueryFunctionImpl
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmServiceFactory
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.repository.SsmChaincodeRepository
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.sign.SsmCmdSigner
import ssm.tx.config.spring.autoconfigure.SsmTxAutoConfiguration

@AutoConfiguration(after = [
    C2ChaincodeAutoConfiguration::class,
    FabricClientConfiguration::class,
    SsmTxAutoConfiguration::class,
])
@ConditionalOnClass(FabricGatewayClient::class)
@ConditionalOnProperty(
    prefix = "coop.fabric",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class FabricSsmStoringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun ssmChaincodeRepository(fabricGatewayClient: FabricGatewayClient): SsmChaincodeRepository =
        FabricSsmClient(fabricGatewayClient)

    @Bean
    @ConditionalOnMissingBean
    fun ssmTxService(
        ssmChaincodeRepository: SsmChaincodeRepository,
        ssmCmdSigner: SsmCmdSigner,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmTxService = SsmServiceFactory(
        ssmChaincodeRepository = ssmChaincodeRepository,
        jsonConverter = JSONConverterObjectMapper(),
        batch = ssmBatchProperties,
    ).buildTxService(ssmCmdSigner)

    @Bean
    @ConditionalOnMissingBean
    fun ssmQueryService(
        ssmChaincodeRepository: SsmChaincodeRepository,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmQueryService = SsmServiceFactory(
        ssmChaincodeRepository = ssmChaincodeRepository,
        jsonConverter = JSONConverterObjectMapper(),
        batch = ssmBatchProperties,
    ).buildQueryService()

    // Upstream SsmChaincodeAutoConfiguration is gated on `ssm.chaincode.url`, which the
    // Fabric-only consumer no longer sets — provide it here so SsmAutomatePersister resolves.
    @Bean
    @ConditionalOnMissingBean
    fun ssmGetSessionLogsQueryFunction(
        ssmQueryService: SsmQueryService,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmGetSessionLogsQueryFunction =
        SsmGetSessionLogsQueryFunctionImpl(ssmBatchProperties, ssmQueryService)
}
