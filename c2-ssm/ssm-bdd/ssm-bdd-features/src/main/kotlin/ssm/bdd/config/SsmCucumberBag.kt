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

    val uuid: String = UUID.randomUUID().toString()

    val clientQuery = SsmServiceFactory
        .builder(config, SsmBatchProperties())
        .buildQueryService()

    val chaincodeUri: ChaincodeUri = ChaincodeUri("chaincode:sandbox:ssm")

    //  SsmSdkTxSteps
    private var currentAdminSigner: SignerAdmin? = null
    var adminSigner: SignerAdmin
        get() = checkNotNull(currentAdminSigner) { "No admin signer has been loaded" }
        set(value) {
            currentAdminSigner = value
        }
    val userSigners: MutableMap<AgentName, SignerUser> = mutableMapOf()
    val ssms: MutableMap<SsmName, Ssm> = mutableMapOf()

    // SsmChaincodeBddSteps
    var adminsName: List<AgentName> = emptyList()
    var usersName: List<AgentName> = emptyList()
    var ssmsName: List<SsmName> = emptyList()
    var sessions: List<String> = emptyList()

    fun clientTx(signer: Signer) = SsmServiceFactory
        .builder(config, SsmBatchProperties())
        .buildTxService(SsmCmdSignerSha256RSASigner(signer))

    companion object {
        val cucumbers: MutableMap<String, SsmCucumberBag> = mutableMapOf()

        fun init(scenario: Scenario): SsmCucumberBag =
            cucumbers.getOrPut(scenario.id) { SsmCucumberBag(SsmSdkConfig(SsmBddConfig.Chaincode.url)) }
    }
}

fun String.contextualize(bag: SsmCucumberBag) = "$this-${bag.uuid}"
