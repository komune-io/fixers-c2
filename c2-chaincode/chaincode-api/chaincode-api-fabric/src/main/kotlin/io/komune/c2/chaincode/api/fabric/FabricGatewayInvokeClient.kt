package io.komune.c2.chaincode.api.fabric

import com.google.rpc.Status
import io.grpc.Metadata
import io.grpc.StatusRuntimeException
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeException
import java.lang.System.currentTimeMillis
import java.util.StringJoiner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hyperledger.fabric.client.EndorseException
import org.hyperledger.fabric.protos.gateway.ErrorDetail
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class FabricGatewayClient(
    private val fabricGatewayBuilder: FabricGatewayBuilder,
) {
    @Suppress("MagicNumber")
//    val customThreadPool = Executors.newFixedThreadPool(1024).asCoroutineDispatcher()
    val parallelIO = Dispatchers.IO.limitedParallelism(256)

    private val logger: Logger = LoggerFactory.getLogger(FabricGatewayClient::class.java)

    @Throws(Exception::class)
    suspend fun query(
        channelId: ChannelId,
        chaincodeId: ChaincodeId,
        invokeArgsList: List<InvokeArgs>
    ): List<String> = coroutineScope {

        val start = currentTimeMillis()
        val proposalResponses = invokeArgsList.map { invokeArgs ->
            async(parallelIO) {
                val contract = fabricGatewayBuilder.contracts(channelId, chaincodeId).shuffled().first()
                val result = contract.evaluateTransaction(invokeArgs.function, *invokeArgs.values.toTypedArray())
                String(result)
            }
        }

        proposalResponses.awaitAll().also {
            logger.info("Transaction[${it.size}] sent in in ${currentTimeMillis() - start} ms")
        }

    }

    @Throws(Exception::class)
    suspend fun invoke(
        channelId: ChannelId,
        chaincodeId: ChaincodeId,
        invokeArgsList: List<InvokeArgs>
    ): List<Transaction> = coroutineScope {
        logger.info("Invoke[${invokeArgsList.size}] transactions in [${channelId}:$chaincodeId]")
        val start = currentTimeMillis()
        val proposal = invokeArgsList.map { invokeArgs ->
                try {
                    val contract = fabricGatewayBuilder.contract(channelId, chaincodeId)
                     contract.newProposal(invokeArgs.function)
                        .addArguments(*invokeArgs.values.toTypedArray())
                        .build()
                        .endorse()
                } catch (e: EndorseException) {
                    val message = extractErrorMessage(e)
                    throw InvokeException(message, e)
                }

        }

        val asyncSubmit = proposal.map { tr ->
            async(parallelIO) {
                val startSubmit = currentTimeMillis()
                logger.info("Submit transaction[${tr.transactionId}] in [${channelId}:$chaincodeId]...")
                tr.submitAsync()
                logger.info("Submitted transaction[${tr.transactionId}] " +
                        "in [${channelId}:$chaincodeId] in ${currentTimeMillis() - startSubmit} ms")
                Transaction(
                    tr.transactionId,
                    tr.result.toString()
                )
            }
        }
        asyncSubmit.awaitAll().also {
            logger.info("Transactions[${invokeArgsList.size}] completed in ${currentTimeMillis() - start} ms")
        }
    }

    private fun extractErrorMessage(e: EndorseException): String {
        val cause = e.cause as StatusRuntimeException
        val grpcStatusDetailsKey =
            Metadata.Key.of("grpc-status-details-bin", Metadata.BINARY_BYTE_MARSHALLER)
        val errors = StringJoiner(";")
        cause.trailers?.get(grpcStatusDetailsKey)?.let {
            val status: Status = Status.parseFrom(it)

            for (detail in status.detailsList) {
                if (detail.typeUrl == "type.googleapis.com/gateway.ErrorDetail") {
                    val details = ErrorDetail.parseFrom(detail.value)
                    errors.add(details.message.replace("chaincode response 500, ", ""))
                }
            }
        }
        return errors.toString()
    }
}

class Transaction(
    val transactionId: String,
    val body: String
)
