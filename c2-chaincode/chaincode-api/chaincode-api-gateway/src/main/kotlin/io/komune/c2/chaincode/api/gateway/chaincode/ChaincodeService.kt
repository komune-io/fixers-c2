package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.fabric.model.InvokeArgs
import io.komune.c2.chaincode.api.fabric.utils.InvokeArgsUtils
import io.komune.c2.chaincode.api.gateway.blockchain.BlockchainService
import io.komune.c2.chaincode.api.gateway.chaincode.model.Cmd
import io.komune.c2.chaincode.api.gateway.chaincode.model.InvokeParams
import io.komune.c2.chaincode.api.gateway.chaincode.model.InvokeReturn
import io.komune.c2.chaincode.api.gateway.chaincode.model.toInvokeArgs
import io.komune.c2.chaincode.api.gateway.config.ChainCodeId
import io.komune.c2.chaincode.api.gateway.config.ChannelId
import io.komune.c2.chaincode.api.gateway.config.FabricClientBuilder
import io.komune.c2.chaincode.api.gateway.config.FabricClientProvider
import io.komune.c2.chaincode.api.gateway.config.HeraclesConfigProps
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.asDeferred
import org.springframework.stereotype.Service

@Service
class ChaincodeService(
	val blockchainService: BlockchainService,
	val coopConfigProps: HeraclesConfigProps,
	val fabricClientProvider: FabricClientProvider,
	val fabricClientBuilder: FabricClientBuilder,
) {

	fun execute(args: InvokeParams): CompletableFuture<String> {
		val chainCodePair = coopConfigProps.getChannelChaincodePair(args.channelid, args.chaincodeid)
		val invokeArgs = args.toInvokeArgs()
		return when (args.cmd) {
			Cmd.invoke -> doInvoke(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
				.thenApply { it.toJson() }
			Cmd.query -> doQuery(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs)
		}
	}

	suspend fun execute(args: List<InvokeParams>): List<Any> {
		val futureList = args.map { params ->
			val chainCodePair = coopConfigProps.getChannelChaincodePair(params.channelid, params.chaincodeid)
			val invokeArgs = InvokeArgs(params.fcn, params.args.iterator())
			when (params.cmd) {
				Cmd.invoke -> doInvoke(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs).asDeferred()
				Cmd.query -> doQuery(chainCodePair.channelId, chainCodePair.chainCodeId, invokeArgs).asDeferred()
			}
		}
		return futureList.awaitAll()
	}

	private fun doQuery(
        channelId: ChannelId,
        chainCodeId: ChainCodeId?,
        invokeArgs: InvokeArgs,
	): CompletableFuture<String> {
		if (InvokeArgsUtils.isBlockQuery(invokeArgs) || InvokeArgsUtils.isTransactionQuery(invokeArgs)) {
			return CompletableFuture.completedFuture(
				blockchainService.query(channelId, invokeArgs)
			)
		}

		return doQueryChaincode(channelId, chainCodeId, invokeArgs)
	}

	private fun doQueryChaincode(
        channelId: ChannelId,
        chainCodeId: ChainCodeId?,
        invokeArgs: InvokeArgs,
	): CompletableFuture<String> {
		val client = fabricClientProvider.get(channelId)
		val channelConfig = fabricClientBuilder.getChannelConfig(channelId)
		val fabricChainCodeClient = fabricClientBuilder.getFabricChainCodeClient(channelId)
		return CompletableFuture.completedFuture(
			fabricChainCodeClient
				.query(channelConfig.endorsers, client, channelId, chainCodeId, invokeArgs)
				.ifBlank { null }
		)
	}

	private fun doInvoke(
        channelId: ChannelId,
        chainCodeId: ChainCodeId,
        invokeArgs: InvokeArgs,
	): CompletableFuture<InvokeReturn> {
		val client = fabricClientProvider.get(channelId)
		val channelConfig = fabricClientBuilder.getChannelConfig(channelId)
		val fabricChainCodeClient = fabricClientBuilder.getFabricChainCodeClient(channelId)
		val future = fabricChainCodeClient.invoke(channelConfig.endorsers, client, channelId, chainCodeId, invokeArgs)
		return future.thenApply {
			InvokeReturn("SUCCESS", "", it.transactionID)
		}
	}
}
