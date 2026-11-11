package io.komune.c2.ssm.fabric.storing

import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.dsl.InvokeFunction
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import io.komune.c2.chaincode.dsl.invoke.toInvokeArgs
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.sdk.core.repository.SsmChaincodeRepository
import ssm.sdk.dsl.CommandOutcome

class FabricSsmClient(
    private val fabricGatewayClient: FabricGatewayClient,
) : SsmChaincodeRepository {

    private val logger = LoggerFactory.getLogger(FabricSsmClient::class.java)

    override suspend fun query(
        cmd: InvokeRequestType,
        fcn: InvokeFunction,
        args: List<String>,
        channelId: ChannelId,
        chaincodeId: ChaincodeId,
    ): String {
        val invokeArgs = InvokeArgs(function = fcn, values = args)
        logger.debug("query [{}:{}] fcn={} args={}", channelId, chaincodeId, fcn, args)
        return fabricGatewayClient.query(channelId, chaincodeId, listOf(invokeArgs)).firstOrNull()
            ?: error("FabricSsmClient.query: empty result for [$channelId:$chaincodeId] fcn=$fcn")
    }

    override suspend fun invoke(
        invokeArgs: List<InvokeRequest>,
        msgIds: List<String>,
    ): List<CommandOutcome> {
        require(invokeArgs.size == msgIds.size) {
            "msgIds.size=${msgIds.size} must match invokeArgs.size=${invokeArgs.size}"
        }
        if (invokeArgs.isEmpty()) return emptyList()
        // Every request is validated before the first one is sent.
        val byTarget = invokeArgs.zip(msgIds).groupBy { (req, _) -> targetOf(req) }

        return byTarget.flatMap { (target, items) ->
            val (channelId, chaincodeId) = target
            logger.info("invoke {} tx(s) on [{}:{}]", items.size, channelId, chaincodeId)
            invokeOn(
                channelId = channelId,
                chaincodeId = chaincodeId,
                args = items.map { (req, _) -> req.toInvokeArgs() },
                ids = items.map { (_, id) -> id },
            )
        }
    }

    private fun targetOf(req: InvokeRequest): Pair<ChannelId, ChaincodeId> {
        val channelId = requireNotNull(req.channelid) { "FabricSsmClient.invoke: InvokeRequest.channelid is required" }
        val chaincodeId = requireNotNull(req.chaincodeid) {
            "FabricSsmClient.invoke: InvokeRequest.chaincodeid is required"
        }
        return channelId to chaincodeId
    }

    // Any transport failure leaves the txs' fate unknown: it becomes Indeterminate outcomes, never an exception.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun invokeOn(
        channelId: ChannelId,
        chaincodeId: ChaincodeId,
        args: List<InvokeArgs>,
        ids: List<String>,
    ): List<CommandOutcome> = try {
        fabricGatewayClient.invoke(
            channelId = channelId,
            chaincodeId = chaincodeId,
            invokeArgsList = args,
            commandIds = ids,
        ).map { it.toCommandOutcome() }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.warn("invoke failed on [{}:{}]: {}", channelId, chaincodeId, e.message)
        ids.map { id ->
            CommandOutcome(
                outcome = "Indeterminate",
                msgId = id,
                errorCode = "TRANSPORT_ERROR",
                errorMessage = e.message ?: e::class.simpleName.orEmpty(),
            )
        }
    }

    private fun TxOutcome.toCommandOutcome(): CommandOutcome = when (this) {
        is TxOutcome.Committed -> CommandOutcome(
            outcome = "Committed", msgId = msgId,
            transactionId = transactionId, blockNumber = blockNumber, payload = payload,
        )
        is TxOutcome.Failure -> CommandOutcome(
            outcome = category.name, msgId = msgId,
            errorCode = errorCode, errorMessage = errorMessage,
        )
    }
}
