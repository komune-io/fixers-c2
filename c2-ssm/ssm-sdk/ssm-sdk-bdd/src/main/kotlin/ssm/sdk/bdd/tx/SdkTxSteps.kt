package ssm.sdk.bdd.tx

import io.cucumber.java8.En
import ssm.bdd.config.SsmCommandStep
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmSession
import ssm.sdk.sign.model.SignerUser

class SdkTxSteps : SsmCommandStep(), En {
	companion object {
		const val GLUE = "ssm.sdk.bdd.tx"
	}

	init {
		prepareSteps()
	}

	override suspend fun startSession(session: SsmSession) {
		bag.clientTx(bag.adminSigner).sendStart(bag.chaincodeUri, session, bag.adminSigner.name)
	}

	override suspend fun createSsm(ssm: Ssm) {
		bag.clientTx(bag.adminSigner).sendCreate(bag.chaincodeUri, ssm, bag.adminSigner.name)
	}

	override suspend fun registerUser(agent: Agent) {
		bag.clientTx(bag.adminSigner).sendRegisterUser(bag.chaincodeUri, agent, bag.adminSigner.name)
	}

	override suspend fun loadSigner(agentName: AgentName, filename: String): SignerUser {
		return SignerUser.loadFromFile(agentName, filename)
	}
}
