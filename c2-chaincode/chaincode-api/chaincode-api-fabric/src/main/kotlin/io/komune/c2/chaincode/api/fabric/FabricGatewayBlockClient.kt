package io.komune.c2.chaincode.api.fabric

import io.komune.c2.chaincode.dsl.BlockId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.IdentitiesInfo
import io.komune.c2.chaincode.dsl.Transaction
import io.komune.c2.chaincode.dsl.TransactionId
import org.hyperledger.fabric.client.Gateway
import org.hyperledger.fabric.protos.common.Block
import org.hyperledger.fabric.protos.common.BlockchainInfo
import org.hyperledger.fabric.protos.common.ChannelHeader
import org.hyperledger.fabric.protos.common.Envelope
import org.hyperledger.fabric.protos.common.Payload
import org.hyperledger.fabric.protos.common.SignatureHeader
import org.hyperledger.fabric.protos.msp.SerializedIdentity
import org.hyperledger.fabric.protos.peer.ProcessedTransaction
import io.komune.c2.chaincode.dsl.Block as BlockDsl
import io.komune.c2.chaincode.dsl.Transaction as TransactionDsl


class FabricGatewayBlockClient(
    private val fabricGatewayBuilder: FabricGatewayBuilder,
) {
//    val GETCHAININFO: String = "GetChainInfo"
//    val GETBLOCKBYNUMBER: String = "GetBlockByNumber"
//    val GETBLOCKBYHASH: String = "GetBlockByHash"
//    val GETTRANSACTIONBYID: String = "GetTransactionByID"
//    val GETBLOCKBYTXID: String = "GetBlockByTxID"


    fun queryAllBlocksIds(channelId: ChannelId): List<Long> {
        val gateway = fabricGatewayBuilder.gateway(channelId)
        return gateway.use {
            val network = gateway.getNetwork(channelId)
            val contract = network.getContract("qscc")
            val blockIds = contract.evaluateTransaction("GetChainInfo", channelId)
            val height = BlockchainInfo.parseFrom(blockIds).height
            (0..height-1).toList()
        }
    }

    fun queryBlockByTransactionId(channelId: ChannelId, transactionId: TransactionId): BlockDsl {
        val gateway = fabricGatewayBuilder.gateway(channelId)
        return gateway.use {
            val network = gateway.getNetwork(channelId)
            val contract = network.getContract("qscc")
            val blockResponse = contract.evaluateTransaction("GetBlockByTxId", channelId, transactionId)
            blockResponse.toBlock()
        }
    }

    fun queryBlockIdByTransactionId(
        gateway: Gateway,
        channelId: ChannelId,
        transactionId: TransactionId
    ): BlockId {
        return gateway.use {
            val network = gateway.getNetwork(channelId)
            val contract = network.getContract("qscc")
            val blockResponse = contract.evaluateTransaction("GetBlockByTxId", channelId, transactionId)
            blockResponse.toBlock().blockId
        }
    }

    fun queryBlockByNumber(channelId: ChannelId, blockId: Long): BlockDsl {
        val gateway = fabricGatewayBuilder.gateway(channelId)
        return gateway.use {
            val network = gateway.getNetwork(channelId)
            val contract = network.getContract("qscc")
            val blockResponse = contract.evaluateTransaction("GetBlockByNumber", channelId, blockId.toString())
            blockResponse.toBlock()
        }
    }

    private fun ByteArray.toBlock(): BlockDsl {
        val result = Block.parseFrom(this)
        val blockId = result.header.number.toInt()
        val transactions = result.data.dataList.map { data ->
            val envelopeInfo: Envelope = Envelope.parseFrom(data)
            val payload = Payload.parseFrom(envelopeInfo.payload)
            payload.asTransaction {blockId}
        }
        return BlockDsl(
            blockId = blockId,
            transactions = transactions,
            previousHash = result.header.previousHash.toByteArray(),
            dataHash = result.header.dataHash.toByteArray()
        )
    }

    fun queryTransactionById(channelId: ChannelId, transactionId: TransactionId): TransactionDsl {
        val gateway = fabricGatewayBuilder.gateway(channelId)
        return gateway.use {
            val network = gateway.getNetwork(channelId)
            val contract = network.getContract("qscc")
            val data = contract.evaluateTransaction("GetTransactionByID", channelId, transactionId)
            val processedTransaction = ProcessedTransaction.parseFrom(data)
            val payload = Payload.parseFrom(processedTransaction.transactionEnvelope.payload)
            payload.asTransaction {
                0
//                queryBlockIdByTransactionId(gateway, channelId, transactionId)
            }
        }
    }

    private fun Payload.asTransaction(getBlockId: () -> BlockId): Transaction {

        val signatureHeader = SignatureHeader.parseFrom(header.signatureHeader)
        val channelHeader = ChannelHeader.parseFrom(header.channelHeader)
        val blockId = getBlockId()
        val identifierHeader = SerializedIdentity.parseFrom(signatureHeader.creator)

        @Suppress("MagicNumber")
        val millis: Long = channelHeader.timestamp.seconds * 1000 + channelHeader.timestamp.nanos / 1000000
        return TransactionDsl(
            transactionId = channelHeader.txId,
            blockId = blockId,
            timestamp = millis,
            isValid = true,
            channelId = channelHeader.channelId,
            creator = IdentitiesInfo(
                mspid = identifierHeader.mspid,
                id = String(identifierHeader.idBytes.toByteArray(), Charsets.UTF_8)
            ),
            nonce = signatureHeader.nonce.toByteArray(),
        )
    }
}
