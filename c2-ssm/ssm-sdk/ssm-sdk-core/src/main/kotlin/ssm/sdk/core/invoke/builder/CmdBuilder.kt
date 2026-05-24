package ssm.sdk.core.invoke.builder

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdName
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.json.JsonUtils
import ssm.sdk.sign.SsmCmdSigner

open class CmdBuilder<T>(
	private val value: T,
	private val commandName: SsmCmdName,
	private val performAction: String? = null) {

	operator fun invoke(chaincodeUri: ChaincodeUri, agentName: AgentName, signer: SsmCmdSigner): SsmCmdSigned {
		val cmd = commandToSign(chaincodeUri, agentName)
		return signer.sign(cmd)
	}

	fun commandToSign(chaincodeUri: ChaincodeUri, agentName: AgentName): SsmCmd {
		val json = JsonUtils.toJson(value)
		val toSign = valueToSign(json)
		return SsmCmd(
			json = json, command = commandName,
			performAction = performAction,
			valueToSign = toSign,
			chaincodeUri = chaincodeUri,
			agentName = agentName
		)
	}

	private fun valueToSign(json: String): String {
		return performAction?.let {
			performAction + json
		} ?: json
	}
}
