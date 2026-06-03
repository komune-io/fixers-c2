package ssm.sdk.core.repository

import io.komune.c2.chaincode.dsl.InvokeFunction
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.sdk.dsl.CommandOutcome

/**
 * Callers must correlate [invoke] results by [CommandOutcome.msgId], not list position —
 * implementations are free to reorder across `(channelId, chaincodeId)` groupings.
 */
interface SsmChaincodeRepository {
	suspend fun query(
		cmd: InvokeRequestType,
		fcn: InvokeFunction,
		args: List<String>,
		channelId: ChannelId,
		chaincodeId: ChaincodeId,
	): String

	suspend fun invoke(
		invokeArgs: List<InvokeRequest>,
		msgIds: List<String>,
	): List<CommandOutcome>
}
