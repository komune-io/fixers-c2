package ssm.bdd.config

import io.cucumber.java8.Scenario
import io.komune.c2.chaincode.dsl.ChaincodeUri
import java.util.UUID
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmName
import ssm.sdk.core.SsmSdkConfig
import ssm.sdk.core.SsmServiceFactory
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.model.Signer
import ssm.sdk.sign.model.SignerAdmin
import ssm.sdk.sign.model.SignerUser

class SsmCucumberBag(
	val config: SsmSdkConfig
) {

	companion object {
		val cucumbers: MutableMap<String, SsmCucumberBag> = mutableMapOf()

		fun init(scenario: Scenario): SsmCucumberBag {
			if (!cucumbers.containsKey(scenario.id)) {
				cucumbers[scenario.id] = SsmCucumberBag(SsmSdkConfig(SsmBddConfig.Chaincode.url)).apply {
					uuid = UUID.randomUUID().toString()
				}
			}
			return cucumbers[scenario.id]!!
		}
	}

	lateinit var uuid: String

	val clientQuery = SsmServiceFactory
		.builder(config, SsmBatchProperties())
		.buildQueryService()

	fun clientTx(signer: Signer) = SsmServiceFactory
		.builder(config, SsmBatchProperties())
		.buildTxService(SsmCmdSignerSha256RSASigner(signer))

	val chaincodeUri: ChaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

	//	SsmSdkTxSteps
	lateinit var adminSigner: SignerAdmin
	var userSigners: MutableMap<AgentName, SignerUser> = mutableMapOf()
	var ssms: MutableMap<SsmName, Ssm> = mutableMapOf()

	// SsmChaincodeBddSteps
	var adminsName: List<AgentName> = emptyList()
	var usersName: List<AgentName> = emptyList()
	var ssmsName: List<SsmName> = emptyList()
	var sessions: List<String> = emptyList()
}

fun String.contextualize(bag: SsmCucumberBag) = "$this-${bag.uuid}"
