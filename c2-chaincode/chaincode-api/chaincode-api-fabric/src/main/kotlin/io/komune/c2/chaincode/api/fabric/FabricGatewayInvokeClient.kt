package io.komune.c2.chaincode.api.fabric

import com.google.rpc.Status
import io.grpc.Metadata
import io.grpc.StatusRuntimeException
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import java.lang.System.currentTimeMillis
import java.util.StringJoiner
import kotlinx.coroutines.CancellationException
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
                val result = contract.evaluateTransaction(invokeArgs.function.value, *invokeArgs.values.toTypedArray())
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
        val results = invokeArgsList.zip(commandIds).map { (args, msgId) ->
            async(parallelIO) {
                runCatching { contracts.random().commitTransaction(channelId, chaincodeId, args, msgId) }
                    .getOrElse { e ->
                        TxOutcome.Transient(
                            msgId = msgId,
                            errorCode = "UNEXPECTED",
                            errorMessage = e.message ?: e::class.simpleName.orEmpty(),
                        )
                    }
            }
        }.awaitAll()

        logger.info("Transactions[${invokeArgsList.size}] completed in ${currentTimeMillis() - start} ms")
        results
    }

    @Suppress("ReturnCount")
    private fun Contract.commitTransaction(
        channelId: ChannelId,
        chaincodeId: ChaincodeId,
        invokeArgs: InvokeArgs,
        msgId: String,
    ): TxOutcome {
        val endorsed = try {
            newProposal(invokeArgs.function.value)
                .addArguments(*invokeArgs.values.toTypedArray())
                .build()
                .endorse()
        } catch (e: EndorseException) {
            return TxOutcome.Rejected(
                msgId = msgId,
                errorCode = "ENDORSE_FAILED",
                errorMessage = extractErrorMessage(e),
            )
        } catch (e: io.grpc.StatusRuntimeException) {
            return TxOutcome.Transient(
                msgId = msgId,
                errorCode = "GRPC_${e.status.code.name}",
                errorMessage = e.message ?: "gRPC failure",
            )
        }

        logger.info("Submit transaction[${endorsed.transactionId}] in [${channelId}:$chaincodeId]...")
        val submitted = try {
            endorsed.submitAsync()
        } catch (e: Exception) {
            return mapSubmitFailure(e, msgId)
        }

        val status = submitted.status
        return if (status.isSuccessful) {
            logger.info(
                "Committed transaction[{}] in [{}:{}] block {}",
                endorsed.transactionId, channelId, chaincodeId, status.blockNumber
            )
            TxOutcome.Committed(
                msgId = msgId,
                transactionId = endorsed.transactionId,
                blockNumber = status.blockNumber,
                payload = String(endorsed.result),
            )
        } else {
            TxValidationCodeMapper.toOutcome(
                msgId = msgId,
                statusCodeName = status.code.name,
                transactionId = endorsed.transactionId,
                blockNumber = status.blockNumber,
            )
        }
    }

    /**
     * Maps a submit-phase exception to the appropriate TxOutcome.
     *
     * - [CancellationException] is rethrown immediately to preserve structured concurrency.
     * - [StatusRuntimeException] (gRPC connectivity failure) → [TxOutcome.Transient] with code
     *   "GRPC_<code>" (e.g. GRPC_UNAVAILABLE, GRPC_DEADLINE_EXCEEDED). The caller may retry.
     * - Any other [Exception] → [TxOutcome.Indeterminate] with code "SUBMIT_FAILED".
     *   The transaction fate is unknown; operator investigation is required.
     */
    internal fun mapSubmitFailure(e: Exception, msgId: String): TxOutcome {
        if (e is CancellationException) throw e
        return when (e) {
            is io.grpc.StatusRuntimeException -> TxOutcome.Transient(
                msgId = msgId,
                errorCode = "GRPC_${e.status.code.name}",
                errorMessage = e.message ?: "gRPC submit failure",
            )
            else -> TxOutcome.Indeterminate(
                msgId = msgId,
                errorCode = "SUBMIT_FAILED",
                errorMessage = e.message ?: "submit failed",
            )
        }
    }

    @Suppress("ReturnCount")
    internal fun extractErrorMessage(e: EndorseException): String {
        val cause = e.cause
        if (cause is StatusRuntimeException) {
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
            val parsed = errors.toString()
            if (parsed.isNotEmpty()) return parsed
            return cause.status.description
                ?: cause.message
                ?: ""
        }
        return e.message ?: e::class.simpleName ?: ""
    }
}

class Transaction(
    val transactionId: String,
    val body: String
)
