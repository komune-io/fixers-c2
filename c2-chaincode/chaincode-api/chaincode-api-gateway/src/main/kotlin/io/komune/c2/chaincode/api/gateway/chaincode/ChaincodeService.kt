package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.config.C2ChaincodeConfiguration
import io.komune.c2.chaincode.api.config.utils.JsonUtils
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeArgsUtils
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.gateway.blockchain.BlockchainServiceI
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.toInvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import org.springframework.stereotype.Service

@Service
class ChaincodeService(
	val fabricGatewayClient: FabricGatewayClient,
	val blockchainService: BlockchainServiceI,
	private val chaincodeConfiguration: C2ChaincodeConfiguration,
) {

	suspend fun execute(args: InvokeRequest): String {
		val chainCodePair = chaincodeConfiguration.getChannelChaincodePair(args.channelid, args.chaincodeid)
		val invokeArgs = args.toInvokeArgs()
		return when (args.cmd) {
			InvokeRequestType.invoke -> doInvoke(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
			InvokeRequestType.query -> doQuery(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
		}
	}

	suspend fun execute(args: List<InvokeRequest>): List<Any> {
		return args.map { params ->
			val chainCodePair = chaincodeConfiguration.getChannelChaincodePair(params.channelid, params.chaincodeid)
			val invokeArgs = InvokeArgs(params.fcn, params.args.toList())
			when (params.cmd) {
				InvokeRequestType.invoke -> doInvoke(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
				InvokeRequestType.query -> doQuery(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
			}
		}
	}

	private suspend fun doQuery(
        channelId: ChannelId,
        chainCodeId: ChaincodeId,
        invokeArgs: InvokeArgs,
	): String {
		return if (InvokeArgsUtils.isBlockQuery(invokeArgs) || InvokeArgsUtils.isTransactionQuery(invokeArgs)) {
			blockchainService.query(channelId, invokeArgs)
		} else {
			doQueryChaincode(channelId, chainCodeId, invokeArgs)
		}
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
	): String {
		return doInvoke(channelId, chainCodeId, listOf(invokeArgs)).first()
			.let (JsonUtils::toJson)
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
		).map {
			InvokeReturn("SUCCESS", "", it.transactionId)
		}
	}

}
