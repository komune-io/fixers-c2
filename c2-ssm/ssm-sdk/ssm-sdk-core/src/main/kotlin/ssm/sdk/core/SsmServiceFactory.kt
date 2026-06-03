package ssm.sdk.core

import java.io.IOException
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.sdk.core.auth.BearerTokenAuthCredentials
import ssm.sdk.core.repository.SsmChaincodeRepository
import ssm.sdk.core.ktor.ChaincodeApiGatewayClient
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.sign.SsmCmdSigner

class SsmServiceFactory(
    private var ssmChaincodeRepository: SsmChaincodeRepository,
    private var jsonConverter: JSONConverterObjectMapper,
    private val batch: SsmBatchProperties,
) {

	fun buildQueryService(): SsmQueryService {
		return SsmQueryService(SsmRequester(jsonConverter, ssmChaincodeRepository))
	}

	fun buildTxService(ssmCmdSigner: SsmCmdSigner): SsmTxService {
		val ssmService = SsmService(SsmRequester(jsonConverter, ssmChaincodeRepository),
			ssmCmdSigner)
		return SsmTxService(ssmService, batch)
	}

	companion object {
		@Throws(IOException::class)
		fun builder(
            filename: String,
            batch: SsmBatchProperties,
            bearerTokenHeaderProvider: BearerTokenAuthCredentials? = null
		): SsmServiceFactory {
			val config = SsmSdkConfig.fromConfigFile(filename)
			return builder(config,batch,  bearerTokenHeaderProvider)
		}

		fun builder(
            config: SsmSdkConfig,
            batch: SsmBatchProperties,
            bearerTokenHeaderProvider: BearerTokenAuthCredentials? = null
		): SsmServiceFactory {
			val gatewayClient = ChaincodeApiGatewayClient(
				baseUrl = config.baseUrl,
				timeout = batch.timeout.toLong(),
				authCredentials = bearerTokenHeaderProvider,
				cloudEventsSource = config.cloudEventsSource,
			)
			val converter = JSONConverterObjectMapper()
			return SsmServiceFactory(
				ssmChaincodeRepository = gatewayClient,
				jsonConverter = converter,
				batch = batch
			)
		}
	}
}
