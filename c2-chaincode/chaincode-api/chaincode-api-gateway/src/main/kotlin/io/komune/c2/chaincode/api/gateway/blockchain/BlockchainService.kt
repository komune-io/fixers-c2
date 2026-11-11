package io.komune.c2.chaincode.api.gateway.blockchain

import io.komune.c2.chaincode.api.config.utils.JsonUtils
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeArgsUtils
import io.komune.c2.chaincode.api.fabric.FabricGatewayBlockClient
import org.springframework.stereotype.Service

@Service
class BlockchainService(
    private val fabricGatewayBlockClient: FabricGatewayBlockClient,

) :BlockchainServiceI {

    override fun query(channelId: ChannelId, invokeArgs: InvokeArgs): String {
        val isList = InvokeArgsUtils.isListQuery(invokeArgs)
        val isBlock = InvokeArgsUtils.isBlockQuery(invokeArgs)
        val isTransaction = InvokeArgsUtils.isTransactionQuery(invokeArgs)
        return when {
            isBlock && isList -> queryAllBlocks(channelId)
            isBlock -> queryBlockByNumber(channelId, invokeArgs)
            isTransaction && isList -> queryAllTransactions(channelId)
            isTransaction -> queryTransactionById(channelId, invokeArgs)
            else -> throw IllegalArgumentException(
                "invokeArgs[${invokeArgs}] must be ${InvokeArgsUtils.BLOCK_QUERY} or " +
                    InvokeArgsUtils.TRANSACTION_QUERY
            )
        }
    }

    override fun queryAllBlocks(channelId: ChannelId): String =
        JsonUtils.toJson(fabricGatewayBlockClient.queryAllBlocksIds(channelId))

    override fun queryBlockByNumber(channelId: ChannelId, invokeArgs: InvokeArgs): String =
        JsonUtils.toJson(fabricGatewayBlockClient.queryBlockByNumber(channelId, invokeArgs.values.first().toLong()))

    override fun queryAllTransactions(channelId: ChannelId): String {
        val transactions = fabricGatewayBlockClient.queryAllBlocksIds(channelId)
            .flatMap { blockId ->
                fabricGatewayBlockClient.queryBlockByNumber(channelId, blockId).transactions
            }
        return JsonUtils.toJson(transactions)
    }

    override fun queryTransactionById(channelId: ChannelId, invokeArgs: InvokeArgs): String =
        JsonUtils.toJson(fabricGatewayBlockClient.queryTransactionById(channelId, invokeArgs.values.first()))
}
