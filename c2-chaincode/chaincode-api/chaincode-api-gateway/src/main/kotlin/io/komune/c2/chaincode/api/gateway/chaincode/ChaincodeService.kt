package io.komune.c2.chaincode.api.gateway.chaincode

import io.komune.c2.chaincode.api.config.C2ChaincodeConfiguration
import io.komune.c2.chaincode.api.config.utils.JsonUtils
import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.api.gateway.blockchain.BlockchainServiceI
import io.komune.c2.chaincode.api.gateway.chaincode.model.OutcomeData
import io.komune.c2.chaincode.api.gateway.config.CloudEventsProperties
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.cloudevent.InvokeEnvelope
import io.komune.c2.chaincode.dsl.cloudevent.InvokeType
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeArgsUtils
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.toInvokeArgs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
open class ChaincodeService(
	val fabricGatewayClient: FabricGatewayClient,
	val blockchainService: BlockchainServiceI,
	private val chaincodeConfiguration: C2ChaincodeConfiguration,
	private val cloudEventsProperties: CloudEventsProperties,
) {

	suspend fun query(args: InvokeRequest): JsonNode {
		val chainCodePair = chaincodeConfiguration.getChannelChaincodePair(args.channelid, args.chaincodeid)
		return doQuery(chainCodePair.channelId, chainCodePair.chainCodeId, args.toInvokeArgs())
	}

	suspend fun execute(args: List<InvokeEnvelope<InvokeRequest>>): List<InvokeEnvelope<OutcomeData>> = supervisorScope {
		args.map { envelope ->
			async { invokeOne(envelope) }
		}.awaitAll()
	}

	private suspend fun invokeOne(envelope: InvokeEnvelope<InvokeRequest>): InvokeEnvelope<OutcomeData> {
		val (type, data) = runCatching { runInvoke(envelope) }
			.getOrElse { e ->
				InvokeType.Outcome.REJECTED to OutcomeData(
					errorCode = "GATEWAY_EXCEPTION",
					errorMessage = e.message ?: e::class.simpleName.orEmpty(),
				)
			}
		return InvokeEnvelope(
			id = UUID.randomUUID().toString(),
			type = type,
			source = cloudEventsProperties.source,
			subject = envelope.id,
			time = OffsetDateTime.now(ZoneOffset.UTC).toString(),
			data = data,
		)
	}

	protected open suspend fun runInvoke(envelope: InvokeEnvelope<InvokeRequest>): Pair<String, OutcomeData> {
		val request = envelope.data
		val pair = chaincodeConfiguration.getChannelChaincodePair(request.channelid, request.chaincodeid)
		val invokeArgs = InvokeArgs(request.fcn, request.args.toList())
		val outcome = fabricGatewayClient.invoke(
			channelId = pair.channelId,
			chaincodeId = pair.chainCodeId,
			invokeArgsList = listOf(invokeArgs),
			commandIds = listOf(envelope.id),
		).first()
		return outcome.toWire()
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
}
