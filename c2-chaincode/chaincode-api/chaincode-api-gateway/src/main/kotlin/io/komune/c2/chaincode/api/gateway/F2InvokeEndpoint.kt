package io.komune.c2.chaincode.api.gateway

import f2.dsl.fnc.F2Function
import f2.dsl.fnc.operators.batch
import io.komune.c2.chaincode.api.config.C2ChaincodeConfiguration
import io.komune.c2.chaincode.api.gateway.chaincode.ChaincodeService
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.toInvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class F2InvokeEndpoint(
	private val chaincodeConfiguration: C2ChaincodeConfiguration,
	private val chaincodeService: ChaincodeService,
) {

    private val logger = LoggerFactory.getLogger(F2InvokeEndpoint::class.java)

	@Bean
	fun invokeF2(): F2Function<InvokeRequest, InvokeReturn> = F2Function { args ->
        args.batch(chaincodeConfiguration.getBatch()) { list ->
			logger.debug("Invoking chaincode ${list.size} items")
			list.groupBy {
				chaincodeConfiguration.getChannelChaincodePair(it.channelid, it.chaincodeid)
			}.flatMap { (channelChainCode, invokeParams) ->
				val channelId = channelChainCode.channelId
				val chainCodeId = channelChainCode.chainCodeId
				val invokeArgs = invokeParams.toInvokeArgs()
				chaincodeService.doInvoke(channelId, chainCodeId, invokeArgs)
			}
		}
	}

}
