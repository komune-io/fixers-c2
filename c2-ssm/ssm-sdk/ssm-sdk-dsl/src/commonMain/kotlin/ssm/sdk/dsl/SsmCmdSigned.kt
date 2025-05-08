package ssm.sdk.dsl

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType

typealias SignerName = String

data class SsmCmdSigned(
    val cmd: SsmCmd,
    val signature: String,
    val signer: SignerName,
    val chaincodeUri: ChaincodeUri,
)

fun SsmCmdSigned.buildArgs(): InvokeArgs {
	return InvokeArgs(
		function = cmd.command.value,
		values = listOfNotNull(cmd.performAction, cmd.json, signer, signature)
	)
}

fun SsmCmdSigned.buildCommandArgs(
	type: InvokeRequestType,
	): InvokeRequest {
	val args = buildArgs()
	return InvokeRequest(
		cmd = type,
		channelid = chaincodeUri.channelId,
		chaincodeid = chaincodeUri.chaincodeId,
		fcn = args.function,
		args = args.values.toTypedArray()
	)
}
