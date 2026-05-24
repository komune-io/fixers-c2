package io.komune.c2.chaincode.api.gateway

import io.komune.c2.chaincode.api.gateway.chaincode.ChaincodeService
import io.komune.c2.chaincode.api.gateway.chaincode.model.OutcomeData
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.cloudevent.InvokeEnvelope
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode

@RestController
@RequestMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ChaincodeRestEndpoint(
	private val chaincodeService: ChaincodeService
) {
	companion object {
		const val CHANNEL_ID_URL_PARAM = "channelid"
		const val CHAINCODE_ID_URL_PARAM = "chaincodeid"
		const val CLOUDEVENTS_BATCH_JSON = "application/cloudevents-batch+json"
	}
	private val logger = LoggerFactory.getLogger(javaClass)

	@GetMapping
	suspend fun query(
		@RequestParam(name = CHANNEL_ID_URL_PARAM, required = false) channel: ChannelId?,
		@RequestParam(name = CHAINCODE_ID_URL_PARAM, required = false) chaincode: ChaincodeId?,
		cmd: InvokeRequestType,
		fcn: String,
		args: Array<String>
	): JsonNode {
		logger.debug("Querying chaincode {}", cmd)
		return chaincodeService.query(InvokeRequest(channel, chaincode, cmd, fcn, args))
	}

	@PostMapping(
		path = ["invoke"],
		consumes = [MediaType.APPLICATION_JSON_VALUE, CLOUDEVENTS_BATCH_JSON],
	)
	suspend fun invoke(
		@RequestBody args: List<InvokeEnvelope<InvokeRequest>>,
	): List<InvokeEnvelope<OutcomeData>> {
		logger.debug("Invoking chaincode {} items", args.size)
		return chaincodeService.execute(args)
	}
}
