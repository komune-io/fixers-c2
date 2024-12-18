package io.komune.c2.chaincode.api.gateway

import f2.dsl.fnc.F2Function
import f2.dsl.fnc.operators.Batch
import f2.dsl.fnc.operators.batch
import io.komune.c2.chaincode.api.fabric.model.InvokeArgs
import io.komune.c2.chaincode.api.gateway.chaincode.model.InvokeParams
import io.komune.c2.chaincode.api.gateway.chaincode.model.InvokeReturn
import io.komune.c2.chaincode.api.gateway.chaincode.model.toInvokeArgs
import io.komune.c2.chaincode.api.gateway.config.ChainCodeId
import io.komune.c2.chaincode.api.gateway.config.ChannelId
import io.komune.c2.chaincode.api.gateway.config.FabricClientBuilder
import io.komune.c2.chaincode.api.gateway.config.FabricClientProvider
import io.komune.c2.chaincode.api.gateway.config.HeraclesConfigProps
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component


@Component
class F2InvokeEndpoint(
	private val fabricClientProvider: FabricClientProvider,
	private val fabricClientBuilder: FabricClientBuilder,
	private val coopConfigProps: HeraclesConfigProps,
) {

    private val logger = LoggerFactory.getLogger(F2InvokeEndpoint::class.java)

	@Bean
	fun invokeF2(): F2Function<InvokeParams, InvokeReturn> = F2Function { args ->
        args.batch(Batch()) { list ->
			logger.debug("Invoking chaincode ${list.size} items")
			list.groupBy {
				coopConfigProps.getChannelChaincodePair(it.channelid, it.chaincodeid)
			}.flatMap { (channelChainCode, invokeParams) ->
				val channelId = channelChainCode.channelId
				val chainCodeId = channelChainCode.chainCodeId
				val invokeArgs = invokeParams.toInvokeArgs()
				doInvoke(channelId, chainCodeId, invokeArgs)
			}
		}
	}

	private suspend fun doInvoke(
		channelId: ChannelId,
		chainCodeId: ChainCodeId,
		invokeArgs: List<InvokeArgs>,
	): List<InvokeReturn> = coroutineScope {
		val client = fabricClientProvider.get(channelId)
		val channelConfig = fabricClientBuilder.getChannelConfig(channelId)
		val fabricChainCodeClientSuspend = fabricClientBuilder.getFabricChainCodeClientSuspend(channelId)
		invokeArgs.let {
			fabricChainCodeClientSuspend.invoke(channelConfig.endorsers, client, channelId, chainCodeId, it).map {
				InvokeReturn("SUCCESS", "", it.transactionID)
			}
		}
	}
}
