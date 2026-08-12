package ssm.chaincode.f2

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmTransition
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmService
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.ktor.SsmRequester
import ssm.sdk.core.repository.SsmChaincodeRepository
import ssm.sdk.json.JSONConverterObjectMapper
import ssm.sdk.json.JsonUtils
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.model.SignerUser

/**
 * Wiring shared by the ssm-tx unit tests: real [SsmTxService]/[SsmQueryService] and a real
 * RSA signer, with only the chaincode repository stubbed out. Signing therefore runs for real,
 * which is the path the BDD suite could only cover against a live sandbox.
 */
internal object SsmTxTestFixtures {

	const val SIGNER_NAME = "adam"
	const val CHANNEL_ID = "sandbox"
	const val CHAINCODE_ID = "ssm"

	val chaincodeUri = ChaincodeUri("chaincode:$CHANNEL_ID:$CHAINCODE_ID")

	/** Generated once: RSA key generation is slow and the key material is irrelevant to the assertions. */
	private val signerKeyPair = SignerUser.generate(SIGNER_NAME)

	val signer = SsmCmdSignerSha256RSASigner(signerKeyPair)

	fun ssm(name: String = "CarDealership") = Ssm(
		name = name,
		transitions = listOf(SsmTransition(from = 0, to = 1, role = "Seller", action = "Sell"))
	)

	fun agent(name: String = "bob") = Agent(name = name, pub = signerKeyPair.pair.public.encoded)

	fun json(value: Any): String = JsonUtils.toJson(value)

	fun txService(repository: SsmChaincodeRepository): SsmTxService =
		SsmTxService(ssmService(repository), SsmBatchProperties())

	fun queryService(repository: SsmChaincodeRepository): SsmQueryService =
		SsmQueryService(requester(repository))

	private fun ssmService(repository: SsmChaincodeRepository): SsmService =
		SsmService(requester(repository), signer)

	private fun requester(repository: SsmChaincodeRepository): SsmRequester =
		SsmRequester(JSONConverterObjectMapper(), repository)
}
