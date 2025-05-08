package ssm.chaincode.f2.features.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.burst
import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.f2.utils.SsmException
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.tx.dsl.features.ssm.SsmInitCommand
import ssm.tx.dsl.features.ssm.SsmInitdResult
import ssm.tx.dsl.features.ssm.SsmTxInitFunction

class SsmTxInitFunctionImpl(
	private val txService: SsmTxService,
	private val queryService: SsmQueryService,
): SsmTxInitFunction {

	override suspend fun invoke(msgs: Flow<SsmInitCommand>): Flow<SsmInitdResult> = msgs.map { payload ->
		val retInitUser = initUser(payload.chaincodeUri.burst(), payload.agent, payload.signerName)
		val retInitSsm = initSsm(payload.chaincodeUri.burst(), payload.ssm, payload.signerName)
		val invoke = listOfNotNull(retInitUser, retInitSsm)
		SsmInitdResult(
			results = invoke.map { it.transactionId }
		)
	}

	private suspend fun initSsm(chaincodeUri: ChaincodeUri, ssm: Ssm, signerName: AgentName): InvokeReturn? {
		return createIfNotExist(ssm,
			{ queryService.getSsm(chaincodeUri, ssm.name) },
			{ this.createSsm(chaincodeUri, it, signerName) }
		)
	}

	private suspend fun initUser(chaincodeUri: ChaincodeUri, user: Agent, signerName: AgentName): InvokeReturn? {
		return createIfNotExist(
			user,
			{ queryService.getAgent(chaincodeUri, user.name) },
			{ this.createUser(chaincodeUri, it, signerName)!! })
	}

	private suspend fun <T> createIfNotExist(
		objToCreate: T,
		getFnc: suspend () -> T?,
		create: suspend (T) -> InvokeReturn,
	): InvokeReturn? {
		return if (getFnc() != null) {
			null
		} else {
			create(objToCreate)
		}
	}

	private suspend fun createSsm(chaincodeUri: ChaincodeUri, ssm: Ssm, signerName: AgentName): InvokeReturn {
		try {
			return txService.sendCreate(chaincodeUri, ssm, signerName)
		} catch (e: Exception) {
			throw SsmException(e)
		}
	}

	private suspend fun createUser(chaincodeUri: ChaincodeUri, agent: Agent, signerName: AgentName): InvokeReturn? {
		try {
			return txService.sendRegisterUser(chaincodeUri, agent, signerName)
		} catch (e: Exception) {
			throw SsmException(e)
		}
	}
}
