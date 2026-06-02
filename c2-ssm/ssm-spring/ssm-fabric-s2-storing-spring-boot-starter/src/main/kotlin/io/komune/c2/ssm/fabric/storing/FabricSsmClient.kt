package io.komune.c2.ssm.fabric.storing

import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.toInvokeArgs
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
        cmd: String,
        fcn: String,
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
        invokeArgs.forEach { req ->
            require(req.channelid != null) { "FabricSsmClient.invoke: InvokeRequest.channelid is required" }
            require(req.chaincodeid != null) { "FabricSsmClient.invoke: InvokeRequest.chaincodeid is required" }
        }

        return invokeArgs.zip(msgIds)
            .groupBy { (req, _) -> req.channelid!! to req.chaincodeid!! }
            .flatMap { (key, items) ->
                val (channelId, chaincodeId) = key
                val args = items.map { (req, _) -> req.toInvokeArgs() }
                val ids = items.map { (_, id) -> id }
                logger.info("invoke {} tx(s) on [{}:{}]", items.size, channelId, chaincodeId)
                runCatching {
                    fabricGatewayClient.invoke(channelId, chaincodeId, args, ids).map { it.toCommandOutcome() }
                }.getOrElse { e ->
                    if (e is kotlinx.coroutines.CancellationException) throw e
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
            }
    }

    private fun TxOutcome.toCommandOutcome(): CommandOutcome = when (this) {
        is TxOutcome.Committed -> CommandOutcome(
            outcome = "Committed", msgId = msgId,
            transactionId = transactionId, blockNumber = blockNumber, payload = payload,
        )
        is TxOutcome.Rejected -> CommandOutcome(
            outcome = "Rejected", msgId = msgId, errorCode = errorCode, errorMessage = errorMessage,
        )
        is TxOutcome.Transient -> CommandOutcome(
            outcome = "Transient", msgId = msgId, errorCode = errorCode, errorMessage = errorMessage,
        )
        is TxOutcome.Indeterminate -> CommandOutcome(
            outcome = "Indeterminate", msgId = msgId, errorCode = errorCode, errorMessage = errorMessage,
        )
        is TxOutcome.Conflict -> CommandOutcome(
            outcome = "Conflict", msgId = msgId, errorCode = errorCode, errorMessage = errorMessage,
        )
    }
}
