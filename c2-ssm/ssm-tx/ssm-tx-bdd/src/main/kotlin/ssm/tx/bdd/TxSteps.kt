package ssm.tx.bdd

import f2.dsl.fnc.invoke
import io.cucumber.java8.En
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import ssm.bdd.config.SsmCommandStep
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.f2.SsmTxAdminServiceImpl
import ssm.chaincode.f2.features.command.SsmTxSessionStartFunctionImpl
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.sign.model.SignerUser
import ssm.tx.dsl.features.ssm.SsmCreateCommand
import ssm.tx.dsl.features.user.SsmUserRegisterCommand
import java.util.UUID


class TxSteps : SsmCommandStep(), En {

	companion object {
		const val GLUE = "ssm.tx.bdd"
	}
	init {
		prepareSteps()

	}

	override suspend fun startSession(session: SsmSession) {
		SsmTxSessionStartFunctionImpl(bag.clientTx(bag.adminSigner)).invoke(
			flowOf(SsmStartCommand(
				msgId = UUID.randomUUID().toString(),
				chaincodeUri = bag.chaincodeUri,
				signerName = bag.adminSigner.name,
				session = session,
			))
		).toList()
	}

	override suspend fun createSsm(ssm: Ssm) {
		getSsmTxAdminServiceImpl().ssmTxCreateFunction().invoke(
			SsmCreateCommand(
				chaincodeUri = bag.chaincodeUri,
				signerName = bag.adminSigner.name,
				ssm = ssm
			)
		)
	}

	override suspend fun registerUser(agent: Agent) {
		getSsmTxAdminServiceImpl().ssmTxUserRegisterFunction().invoke(
			SsmUserRegisterCommand(
				chaincodeUri = bag.chaincodeUri,
				signerName = bag.adminSigner.name,
				agent
			)
		)
	}

	override suspend fun loadSigner(agentName: AgentName, filename: String): SignerUser {
		return SignerUser.loadFromFile(agentName, filename)
	}

	private fun getSsmTxAdminServiceImpl() = SsmTxAdminServiceImpl(
		ssmTxService = bag.clientTx(bag.adminSigner),
		ssmQueryService = bag.clientQuery,
	)
}
