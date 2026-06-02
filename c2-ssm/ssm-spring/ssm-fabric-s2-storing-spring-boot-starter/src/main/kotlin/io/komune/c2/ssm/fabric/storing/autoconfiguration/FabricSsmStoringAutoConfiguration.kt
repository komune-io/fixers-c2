package io.komune.c2.ssm.fabric.storing.autoconfiguration

import io.komune.c2.chaincode.api.config.autoconfiguration.C2ChaincodeAutoConfiguration
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.autoconfiguration.FabricClientConfiguration
import io.komune.c2.ssm.fabric.storing.FabricRepository
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
import ssm.sdk.core.repository.SsmRequesterRepository
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.sign.SsmCmdSigner
import ssm.tx.config.spring.autoconfigure.SsmTxAutoConfiguration

/**
 * Wires the in-process Fabric transport into the existing SSM bean graph.
 *
 * What this autoconfig provides:
 * - `SsmRequesterRepository` -> [FabricRepository] (wraps [FabricGatewayClient]).
 * - `SsmTxService` and `SsmQueryService` -> built via [SsmServiceFactory] using the Fabric
 *   repository above.
 *
 * What it deliberately does NOT provide:
 * - `SsmCmdSigner` / `SsmBatchProperties`: consumed from [SsmTxAutoConfiguration]. The signer
 *   reads `ssm.signer.admin.*` properties — the consumer keeps those.
 * - The four F2 functions (`SsmTxSessionStartFunction`, `SsmTxSessionPerformActionFunction`,
 *   `SsmTxInitFunction`, `SsmGetSessionLogsQueryFunction`): provided by the sibling tx /
 *   chaincode starters, which autowire `SsmTxService` and `SsmQueryService`. Once our beans
 *   are present, those starters' impls just work.
 *
 * Coexistence with the ktor path:
 * - [SsmTxAutoConfiguration] only produces `SsmTxService` / `SsmQueryService` when
 *   `ssm.chaincode.url` is set (via `SsmChaincodeProperties`). The Phase 2 consumer change
 *   removes that property, so the ktor beans never materialise and our `@ConditionalOnMissingBean`
 *   beans win unopposed.
 * - `@ConditionalOnClass(FabricGatewayClient::class)` keeps this autoconfig dormant if the
 *   `chaincode-api-fabric` artifact is absent from the classpath.
 * - `coop.fabric.enabled=false` is an emergency kill switch.
 */
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
    fun ssmRequesterRepository(fabricGatewayClient: FabricGatewayClient): SsmRequesterRepository =
        FabricRepository(fabricGatewayClient)

    @Bean
    @ConditionalOnMissingBean
    fun ssmTxService(
        ssmRequesterRepository: SsmRequesterRepository,
        ssmCmdSigner: SsmCmdSigner,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmTxService = SsmServiceFactory(
        ssmRequesterRepository = ssmRequesterRepository,
        jsonConverter = JSONConverterObjectMapper(),
        batch = ssmBatchProperties,
    ).buildTxService(ssmCmdSigner)

    @Bean
    @ConditionalOnMissingBean
    fun ssmQueryService(
        ssmRequesterRepository: SsmRequesterRepository,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmQueryService = SsmServiceFactory(
        ssmRequesterRepository = ssmRequesterRepository,
        jsonConverter = JSONConverterObjectMapper(),
        batch = ssmBatchProperties,
    ).buildQueryService()

    /**
     * Provided here because the upstream SsmChaincodeAutoConfiguration that normally produces
     * this F2 bean is gated on `ssm.chaincode.url` (the old HTTP transport property), which
     * the consumer no longer sets. SsmAutomatePersister autowires this bean to load session
     * iteration state during persist().
     */
    @Bean
    @ConditionalOnMissingBean
    fun ssmGetSessionLogsQueryFunction(
        ssmQueryService: SsmQueryService,
        ssmBatchProperties: SsmBatchProperties,
    ): SsmGetSessionLogsQueryFunction =
        SsmGetSessionLogsQueryFunctionImpl(ssmBatchProperties, ssmQueryService)
}
