package ssm.sdk.core

import java.io.IOException
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.sdk.core.auth.BearerTokenAuthCredentials
import ssm.sdk.core.ktor.KtorRepository
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.sign.SsmCmdSigner

class SsmServiceFactory(
    private var coopRepository: KtorRepository,
    private var jsonConverter: JSONConverterObjectMapper,
    private val batch: SsmBatchProperties,
) {

	fun buildQueryService(): SsmQueryService {
		return SsmQueryService(SsmRequester(jsonConverter, coopRepository))
	}

	fun buildTxService(ssmCmdSigner: SsmCmdSigner): SsmTxService {
		val ssmService = SsmService(SsmRequester(jsonConverter, coopRepository),
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
			val coopRepository = KtorRepository(config.baseUrl, batch.timeout.toLong(), bearerTokenHeaderProvider)
			val converter = JSONConverterObjectMapper()
			return SsmServiceFactory(
				coopRepository = coopRepository,
				jsonConverter = converter,
				batch = batch
			)
		}
	}
}
