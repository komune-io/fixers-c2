package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.config.C2ChaincodeConfiguration
import io.komune.c2.chaincode.api.config.utils.JsonUtils
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeArgsUtils
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.api.gateway.blockchain.BlockchainServiceI
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.toInvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode

@Service
class ChaincodeService(
	val fabricGatewayClient: FabricGatewayClient,
	val blockchainService: BlockchainServiceI,
	private val chaincodeConfiguration: C2ChaincodeConfiguration,
) {

	suspend fun execute(args: InvokeRequest): JsonNode {
		val chainCodePair = chaincodeConfiguration.getChannelChaincodePair(args.channelid, args.chaincodeid)
		val invokeArgs = args.toInvokeArgs()
		return when (args.cmd) {
			InvokeRequestType.invoke -> JsonUtils.valueToTree(
				doInvoke(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
			)
			InvokeRequestType.query -> doQuery(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
		}
	}

	suspend fun execute(args: List<InvokeRequest>): List<JsonNode> = coroutineScope {
		args.map { params ->
			async {
				val chainCodePair = chaincodeConfiguration.getChannelChaincodePair(
					params.channelid, params.chaincodeid
				)
				val invokeArgs = InvokeArgs(params.fcn, params.args.toList())
				when (params.cmd) {
					InvokeRequestType.invoke -> JsonUtils.valueToTree(
						doInvoke(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
					)
					InvokeRequestType.query -> doQuery(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
				}
			}
		}.awaitAll()
	}

	private suspend fun doQuery(
        channelId: ChannelId,
        chainCodeId: ChaincodeId,
        invokeArgs: InvokeArgs,
	): JsonNode {
		val raw = if (InvokeArgsUtils.isBlockQuery(invokeArgs) || InvokeArgsUtils.isTransactionQuery(invokeArgs)) {
			blockchainService.query(channelId, invokeArgs)
		} else {
			doQueryChaincode(channelId, chainCodeId, invokeArgs)
		}
		return JsonUtils.toNode(raw)
	}

	private suspend fun doQueryChaincode(
        channelId: ChannelId,
        chainCodeId: ChaincodeId,
        invokeArgs: InvokeArgs,
	): String {
		return fabricGatewayClient.query(
			channelId = channelId,
			chaincodeId = chainCodeId,
			invokeArgsList = listOf(invokeArgs)
		).first()
	}

	private suspend fun doInvoke(
        channelId: ChannelId,
        chainCodeId: ChaincodeId,
        invokeArgs: InvokeArgs,
	): InvokeReturn {
		return doInvoke(channelId, chainCodeId, listOf(invokeArgs)).first()
	}

	suspend fun doInvoke(
        channelId: ChannelId,
        chainCodeId: ChaincodeId,
        invokeArgs: List<InvokeArgs>,
	): List<InvokeReturn> {
		return fabricGatewayClient.invoke(
			channelId = channelId,
			chaincodeId = chainCodeId,
			invokeArgsList = invokeArgs
		).map { outcome ->
			when (outcome) {
				is TxOutcome.Committed -> InvokeReturn("SUCCESS", "", outcome.transactionId)
				is TxOutcome.Rejected -> InvokeReturn("ERROR", "${outcome.errorCode}: ${outcome.errorMessage}", "")
				is TxOutcome.Transient -> InvokeReturn("ERROR", "${outcome.errorCode}: ${outcome.errorMessage}", "")
				is TxOutcome.Indeterminate -> InvokeReturn("ERROR", "${outcome.errorCode}: ${outcome.errorMessage}", "")
				is TxOutcome.Conflict -> InvokeReturn("ERROR", "${outcome.errorCode}: ${outcome.errorMessage}", "")
			}
		}
	}

}
