package io.komune.c2.chaincode.api.fabric

import io.komune.c2.chaincode.api.fabric.config.FabricConfig
import io.komune.c2.chaincode.api.fabric.exception.InvokeException
import io.komune.c2.chaincode.api.fabric.factory.FabricChannelFactory
import io.komune.c2.chaincode.api.fabric.model.Endorser
import io.komune.c2.chaincode.api.fabric.model.InvokeArgs
import java.io.IOException
import java.lang.System.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.asDeferred
import org.hyperledger.fabric.sdk.BlockEvent
import org.hyperledger.fabric.sdk.ChaincodeID
import org.hyperledger.fabric.sdk.ChaincodeResponse
import org.hyperledger.fabric.sdk.Channel
import org.hyperledger.fabric.sdk.HFClient
import org.hyperledger.fabric.sdk.TransactionProposalRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FabricChainCodeClientSuspend(private val channelFactory: FabricChannelFactory) {

    private val logger: Logger = LoggerFactory.getLogger(FabricChainCodeClient::class.java)

    companion object {
        @Throws(IOException::class)
        fun fromConfigFile(filename: String?, cryptoConfigBase: String?): FabricChainCodeClientSuspend {
            val fabricConfig = FabricConfig.loadFromFile(filename)
            val channelFactory = FabricChannelFactory.factory(fabricConfig, cryptoConfigBase)

            return FabricChainCodeClientSuspend(channelFactory)
        }
    }

    @Throws(Exception::class)
    suspend fun invoke(
        endorsers: List<Endorser>,
        client: HFClient,
        channelName: String,
        chainId: String,
        invokeArgsList: List<InvokeArgs>
    ): List<BlockEvent.TransactionEvent> {
        val start = currentTimeMillis()
        val channel = channelFactory.getChannel(endorsers, client, channelName)
        val chaincodeId = ChaincodeID.newBuilder().setName(chainId).build()
        return invokeBlockChain(client, channel, chaincodeId, invokeArgsList).let {
            logger.info("Transactions[${invokeArgsList.size}] completed in ${currentTimeMillis() - start} ms")
            it
        }
    }

    private suspend fun invokeBlockChain(
        client: HFClient, channel: Channel, chainCodeId: ChaincodeID, invokeArgsList: List<InvokeArgs>
    ): List<BlockEvent.TransactionEvent> = coroutineScope {
        val proposalResponses = invokeArgsList.mapIndexed { index, invokeArgs ->
            async(Dispatchers.IO) {
                try {
                    val proposalRequest = buildTransactionProposalRequest(client, chainCodeId, invokeArgs)
                    channel.sendTransactionProposal(proposalRequest, channel.peers)
                } catch (e: Exception) {
                    throw InvokeException("Failed to send proposal for ${invokeArgs.function}", e)
                }
            }
        }
        val start = currentTimeMillis()
        val allResponses = proposalResponses.awaitAll()
        logger.info(
            "TransactionProposalRequests[${allResponses.size}] all completed in ${currentTimeMillis() - start} ms"
        )
        val errors = allResponses.flatten().filterNot {
            it.status == ChaincodeResponse.Status.SUCCESS
        }

        if (errors.isNotEmpty()) {
            val errorMessages = errors.map { "Peer ${it.peer.name} failed: ${it.message}" }
            logger.error("Transaction failed: ${errorMessages.joinToString(", ")}")
            throw InvokeException(errorMessages)
        }

        logger.info("All proposals succeeded. Sending transaction to orderer...")
        val startSend = currentTimeMillis()
        allResponses.map { response ->
            channel.sendTransaction(response).asDeferred()
        }.awaitAll().also {
            logger.info("Transaction[${allResponses.size}] sent in in ${currentTimeMillis() - startSend} ms")
        }
    }

    private fun buildTransactionProposalRequest(
        client: HFClient,
        chainCodeId: ChaincodeID,
        invokeArgs: InvokeArgs
    ): TransactionProposalRequest {
        return client.newTransactionProposalRequest().apply {
            chaincodeID = chainCodeId
            fcn = invokeArgs.function
            args = invokeArgs.values
        }
    }

}
