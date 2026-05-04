package io.komune.c2.chaincode.api.gateway

import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.api.gateway.chaincode.ChaincodeService
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ChaincodeRestEndpoint(
	private val chaincodeService: ChaincodeService
) {
	companion object {
		const val CHANNEL_ID_URL_PARAM = "channelid"
		const val CHAINCODE_ID_URL_PARAM = "chaincodeid"
	}
	private val logger = LoggerFactory.getLogger(javaClass)

	@GetMapping
	suspend fun query(
		@RequestParam(name = CHANNEL_ID_URL_PARAM, required = false) channel: ChannelId?,
		@RequestParam(name = CHAINCODE_ID_URL_PARAM, required = false) chaincode: ChaincodeId?,
		cmd: InvokeRequestType,
		fcn: String,
		args: Array<String>
	): String {
		logger.debug("Querying chaincode $cmd")
		return chaincodeService.execute(InvokeRequest(channel, chaincode, cmd, fcn, args))
	}

	@PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
	suspend fun invoke(
		@ModelAttribute args: InvokeRequest
	): String {
		logger.debug("Invoking chaincode ${args.cmd}")
		return chaincodeService.execute(args)
	}

	@PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
	suspend fun invokeJson(
		@RequestBody args: InvokeRequest
	): String {
		logger.debug("Invoking chaincode ${args.cmd}")
		return chaincodeService.execute(args)
	}

	@PostMapping(path = ["invoke"], consumes = [MediaType.APPLICATION_JSON_VALUE])
	suspend fun invokeJson(
		@RequestBody args: List<InvokeRequest>
	): List<Any> {
		logger.debug("Invoking chaincode ${args.size} items")
		return chaincodeService.execute(args)
	}

}
