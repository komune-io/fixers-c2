package ssm.sdk.core.repository

import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.sdk.dsl.CommandOutcome

/**
 * Transport-agnostic seam for [ssm.sdk.core.ktor.SsmRequester]. Implementations submit
 * signed SSM invocations to a chaincode backend and forward chaincode queries.
 *
 * Today there are two implementations:
 * - [ssm.sdk.core.ktor.KtorRepository] — HTTP via the c2-chaincode-api-gateway REST API.
 * - `io.komune.c2.ssm.fabric.storing.FabricRepository` (ssm-fabric-s2-storing-spring-boot-starter)
 *   — in-process via the Hyperledger Fabric Gateway Java SDK.
 *
 * Both methods preserve `msgId` ordering only via the explicit `msgIds` / outcome keys;
 * callers must key by `CommandOutcome.msgId`, never by list position.
 */
interface SsmRequesterRepository {
	suspend fun query(
		cmd: String,
		fcn: String,
		args: List<String>,
		channelId: ChannelId?,
		chaincodeId: ChaincodeId?,
	): String

	suspend fun invoke(
		invokeArgs: List<InvokeRequest>,
		msgIds: List<String>,
	): List<CommandOutcome>
}
