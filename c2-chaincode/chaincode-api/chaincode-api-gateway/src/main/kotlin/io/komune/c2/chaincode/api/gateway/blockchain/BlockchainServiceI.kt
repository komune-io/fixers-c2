package io.komune.c2.chaincode.api.gateway.blockchain

import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs

interface BlockchainServiceI {
	fun query(channelId: ChannelId, invokeArgs: InvokeArgs): String
	fun queryAllBlocks(channelId: ChannelId): String
	fun queryBlockByNumber(channelId: ChannelId, invokeArgs: InvokeArgs): String
	fun queryAllTransactions(channelId: ChannelId): String
	fun queryTransactionById(channelId: ChannelId, invokeArgs: InvokeArgs): String
}
