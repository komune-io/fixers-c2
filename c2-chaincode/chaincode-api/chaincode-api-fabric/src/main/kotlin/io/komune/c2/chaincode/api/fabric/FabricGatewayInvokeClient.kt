package io.komune.c2.chaincode.api.fabric

import com.google.rpc.Status
import io.grpc.Metadata
import io.grpc.StatusRuntimeException
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import java.lang.System.currentTimeMillis
import java.util.StringJoiner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import org.hyperledger.fabric.client.Contract
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
        invokeArgsList: List<InvokeArgs>,
        commandIds: List<String> = invokeArgsList.indices.map { "auto-$it" },
    ): List<TxOutcome> = supervisorScope {
        require(commandIds.size == invokeArgsList.size) {
            "commandIds.size=${commandIds.size} must match invokeArgsList.size=${invokeArgsList.size}"
        }
        logger.info("Invoke[${invokeArgsList.size}] transactions in [${channelId}:$chaincodeId]")
        val start = currentTimeMillis()

        val contracts = fabricGatewayBuilder.contracts(channelId, chaincodeId)
        val results = invokeArgsList.zip(commandIds).map { (args, commandId) ->
            async(parallelIO) {
                runCatching { contracts.random().commitTransaction(channelId, chaincodeId, args, commandId) }
                    .getOrElse { e ->
                        TxOutcome.Transient(
                            commandId = commandId,
                            errorCode = "UNEXPECTED",
                            errorMessage = e.message ?: e::class.simpleName.orEmpty(),
                        )
                    }
            }
        }.awaitAll()

        logger.info("Transactions[${invokeArgsList.size}] completed in ${currentTimeMillis() - start} ms")
        results
    }

    private fun Contract.commitTransaction(
        channelId: ChannelId,
        chaincodeId: ChaincodeId,
        invokeArgs: InvokeArgs,
        commandId: String,
    ): TxOutcome {
        val endorsed = try {
            newProposal(invokeArgs.function)
                .addArguments(*invokeArgs.values.toTypedArray())
                .build()
                .endorse()
        } catch (e: EndorseException) {
            return TxOutcome.Rejected(
                commandId = commandId,
                errorCode = "ENDORSE_FAILED",
                errorMessage = extractErrorMessage(e),
            )
        } catch (e: io.grpc.StatusRuntimeException) {
            return TxOutcome.Transient(
                commandId = commandId,
                errorCode = "GRPC_${e.status.code.name}",
                errorMessage = e.message ?: "gRPC failure",
            )
        }

        logger.info("Submit transaction[${endorsed.transactionId}] in [${channelId}:$chaincodeId]...")
        val submitted = try {
            endorsed.submitAsync()
        } catch (e: Exception) {
            return TxOutcome.Indeterminate(
                commandId = commandId,
                errorCode = "SUBMIT_FAILED",
                errorMessage = e.message ?: "submit failed",
            )
        }

        val status = submitted.status
        return if (status.isSuccessful) {
            logger.info(
                "Committed transaction[{}] in [{}:{}] block {}",
                endorsed.transactionId, channelId, chaincodeId, status.blockNumber
            )
            TxOutcome.Committed(
                commandId = commandId,
                transactionId = endorsed.transactionId,
                blockNumber = status.blockNumber,
                payload = String(endorsed.result),
            )
        } else {
            TxOutcome.Conflict(
                commandId = commandId,
                errorCode = status.code.name,
                errorMessage = "Transaction[${endorsed.transactionId}] failed to commit: code=${status.code}",
                transactionId = endorsed.transactionId,
                blockNumber = status.blockNumber,
            )
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
