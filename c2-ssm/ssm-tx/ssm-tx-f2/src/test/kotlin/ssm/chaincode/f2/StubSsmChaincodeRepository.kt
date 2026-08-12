package ssm.chaincode.f2

import io.komune.c2.chaincode.dsl.InvokeFunction
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.sdk.core.repository.SsmChaincodeRepository
import ssm.sdk.dsl.CommandOutcome

/**
 * In-memory [SsmChaincodeRepository] standing in for the chaincode gateway, mirroring the
 * stubbed-repository pattern used by `ssm-sdk-core` tests. Every query and invoke is recorded so
 * tests can assert on what the transaction layer actually emitted. No Docker, sandbox or network.
 *
 * Query responses are scripted per `fcn:firstArg` key. Each call consumes one entry, so a
 * `createIfNotExist`-style "check, then re-check" sequence can be driven precisely. An exhausted or
 * missing key yields [NOT_FOUND] (empty body), which the SDK converts to `null`.
 */
internal class StubSsmChaincodeRepository(
	responses: Map<String, List<String>> = emptyMap(),
	private val onInvoke: (List<InvokeRequest>, List<String>) -> List<CommandOutcome> = { _, msgIds ->
		msgIds.map { CommandOutcome(outcome = "Committed", msgId = it, transactionId = "tx-$it") }
	},
) : SsmChaincodeRepository {

	data class QueryCall(
		val fcn: String,
		val args: List<String>,
		val channelId: ChannelId,
		val chaincodeId: ChaincodeId,
	)

	private val scripted = responses.mapValues { (_, bodies) -> ArrayDeque(bodies) }

	val queries = mutableListOf<QueryCall>()
	val invoked = mutableListOf<InvokeRequest>()
	val invokedMsgIds = mutableListOf<List<String>>()

	override suspend fun query(
		cmd: InvokeRequestType,
		fcn: InvokeFunction,
		args: List<String>,
		channelId: ChannelId,
		chaincodeId: ChaincodeId,
	): String {
		queries += QueryCall(fcn.value, args, channelId, chaincodeId)
		return scripted[key(fcn.value, args.firstOrNull())]?.removeFirstOrNull() ?: NOT_FOUND
	}

	override suspend fun invoke(invokeArgs: List<InvokeRequest>, msgIds: List<String>): List<CommandOutcome> {
		invoked += invokeArgs
		invokedMsgIds += msgIds
		return onInvoke(invokeArgs, msgIds)
	}

	companion object {
		/** The gateway returns an empty body for a missing entry. */
		const val NOT_FOUND = ""

		fun key(fcn: String, value: String?) = "$fcn:$value"
	}
}
