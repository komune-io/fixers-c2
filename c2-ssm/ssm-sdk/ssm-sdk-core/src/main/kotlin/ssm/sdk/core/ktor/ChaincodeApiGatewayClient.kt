package ssm.sdk.core.ktor

import io.komune.c2.chaincode.dsl.InvokeFunction
import io.komune.c2.chaincode.dsl.cloudevent.InvokeEnvelope
import io.komune.c2.chaincode.dsl.cloudevent.InvokeType
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.sdk.core.auth.AuthCredentials
import ssm.sdk.core.auth.BearerTokenAuthCredentials
import ssm.sdk.core.repository.SsmChaincodeRepository
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.json.JsonUtils
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ChaincodeApiGatewayClient(
    private val baseUrl: String,
    timeout: Long,
    private val authCredentials: AuthCredentials?,
    private val cloudEventsSource: String = DEFAULT_CLOUDEVENTS_SOURCE,
    client: HttpClient? = null,
) : SsmChaincodeRepository {
    private val logger = LoggerFactory.getLogger(javaClass)

    // The client lives as long as this repository and is shared by every call: no scope to close it in.
    @Suppress("MissingUseCall")
    val client = client ?: HttpClient(CIO) {
        if(logger.isDebugEnabled) {
            install(Logging)
        }
        install(ContentNegotiation) {
            jackson()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = timeout
            connectTimeoutMillis = timeout
        }
    }

    override suspend fun query(
        cmd: InvokeRequestType,
        fcn: InvokeFunction,
        args: List<String>,
        channelId: ChannelId,
        chaincodeId: ChaincodeId,
    ): String {
        return client.get(baseUrl + PATH) {
            addAuth()
            parameter(key = CMD_PROPS, value = cmd.name)
            parameter(key = CHANNEL_ID_PROPS, value = channelId)
            parameter(key = CHAINCODE_ID_PROPS, value = chaincodeId)
            parameter(key = FCN_PROPS, value = fcn.value)
            parameter(key = ARGS_PROPS, value = args.first())
        }.bodyAsText()
    }

    suspend fun getBlock(blockId: Long, channelId: ChannelId?): String {
        return client.get(baseUrl) {
            addAuth()

            if (channelId != null) parameter(key = "channelId", value = channelId)
            url {
                path("blocks", blockId.toString())
            }
        }.bodyAsText()
    }

    suspend fun getTransaction(txId: String, channelId: ChannelId?): String {
        return client.get(baseUrl) {
            addAuth()
            if (channelId != null) parameter(key = "channelId", value = channelId)
            url {
                path("transactions", txId)
            }
        }.bodyAsText()
    }

    override suspend fun invoke(
        invokeArgs: List<InvokeRequest>,
        msgIds: List<String>,
    ): List<CommandOutcome> {
        require(invokeArgs.size == msgIds.size) {
            "msgIds.size=${msgIds.size} must match invokeArgs.size=${invokeArgs.size}"
        }
        val body = invokeArgs.zip(msgIds).map { (req, msgId) -> buildEnvelope(msgId, req) }
        // Whatever fails between send and answer leaves the txs' fate unknown: Indeterminate outcomes, never a throw.
        @Suppress("TooGenericExceptionCaught")
        return try {
            val response = client.post("$baseUrl$INVOKE_PATH") {
                addAuth()
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            mapHttpResponse(response, msgIds)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            mapNetworkError(e, msgIds)
        }
    }

    private fun buildEnvelope(msgId: String, req: InvokeRequest): InvokeEnvelope<InvokeRequest> =
        InvokeEnvelope(
            id = msgId,
            type = InvokeType.Request.GENERIC,
            source = cloudEventsSource,
            time = OffsetDateTime.now(ZoneOffset.UTC).toString(),
            data = req,
        )

    private suspend fun mapHttpResponse(
        response: HttpResponse,
        msgIds: List<String>,
    ): List<CommandOutcome> {
        val statusValue = response.status.value
        return when {
            response.status.isSuccess() -> decodeOutcomes(response.bodyAsText(), msgIds)
            statusValue in CLIENT_ERROR_RANGE -> synthesiseOutcomes(
                msgIds = msgIds,
                outcome = OUTCOME_REJECTED,
                errorCode = "HTTP_$statusValue",
                errorMessage = response.bodyAsText(),
            )
            statusValue in SERVER_ERROR_RANGE -> synthesiseOutcomes(
                msgIds = msgIds,
                outcome = OUTCOME_TRANSIENT,
                errorCode = "HTTP_$statusValue",
                errorMessage = response.bodyAsText(),
            )
            else -> synthesiseOutcomes(
                msgIds = msgIds,
                outcome = OUTCOME_INDETERMINATE,
                errorCode = "UNEXPECTED_HTTP_$statusValue",
                errorMessage = response.bodyAsText(),
            )
        }
    }

    private fun decodeOutcomes(body: String, msgIds: List<String>): List<CommandOutcome> {
        val envelopes: List<InvokeEnvelope<OutcomeWire>> = JsonUtils.toObject(body)
        return envelopes.map { it.toCommandOutcome() }
            .also { decoded ->
                if (decoded.size != msgIds.size) {
                    logger.warn(
                        "invoke response size mismatch: expected={}, got={} (msgIds={})",
                        msgIds.size, decoded.size, msgIds,
                    )
                }
            }
    }

    private fun mapNetworkError(e: Throwable, msgIds: List<String>): List<CommandOutcome> {
        val code = when (e) {
            is HttpRequestTimeoutException,
            is ConnectTimeoutException,
            is SocketTimeoutException -> "TIMEOUT"
            is ConnectException -> "CONNECT_REFUSED"
            else -> "TRANSPORT_ERROR"
        }
        logger.warn("invoke network error ({}) for msgIds={}: {}", code, msgIds, e.message)
        return synthesiseOutcomes(
            msgIds = msgIds,
            outcome = OUTCOME_INDETERMINATE,
            errorCode = code,
            errorMessage = e.message,
        )
    }

    private fun synthesiseOutcomes(
        msgIds: List<String>,
        outcome: String,
        errorCode: String,
        errorMessage: String?,
    ): List<CommandOutcome> = msgIds.map { msgId ->
        CommandOutcome(
            outcome = outcome,
            msgId = msgId,
            errorCode = errorCode,
            errorMessage = errorMessage,
        )
    }

    private fun HttpRequestBuilder.addAuth() {
        if (authCredentials is BearerTokenAuthCredentials) {
            header(key = "Authorization", value = "Bearer ${authCredentials.getBearerToken()}")
        }
    }

    companion object {
        const val PATH = "/"
        const val INVOKE_PATH = "/invoke"
        const val DEFAULT_CLOUDEVENTS_SOURCE = "/io.komune.c2/sdk"
        val CMD_PROPS = InvokeRequest::cmd.name
        val CHANNEL_ID_PROPS = InvokeRequest::channelid.name
        val CHAINCODE_ID_PROPS = InvokeRequest::chaincodeid.name
        val FCN_PROPS = InvokeRequest::fcn.name
        val ARGS_PROPS = InvokeRequest::args.name
        val CLIENT_ERROR_RANGE = 400..499
        val SERVER_ERROR_RANGE = 500..599
    }
}

private const val OUTCOME_REJECTED = "Rejected"
private const val OUTCOME_TRANSIENT = "Transient"
private const val OUTCOME_INDETERMINATE = "Indeterminate"

internal data class OutcomeWire(
    val transactionId: String? = null,
    val blockNumber: Long? = null,
    val payload: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)

internal fun InvokeEnvelope<OutcomeWire>.toCommandOutcome(): CommandOutcome {
    val outcomeStr = when (type) {
        InvokeType.Outcome.COMMITTED -> "Committed"
        InvokeType.Outcome.REJECTED -> "Rejected"
        InvokeType.Outcome.TRANSIENT -> "Transient"
        InvokeType.Outcome.INDETERMINATE -> "Indeterminate"
        InvokeType.Outcome.CONFLICT -> "Conflict"
        else -> error("Unknown CloudEvent outcome type: $type")
    }
    return CommandOutcome(
        outcome = outcomeStr,
        msgId = subject ?: error("CloudEvent response is missing subject (request id correlation)"),
        transactionId = data.transactionId,
        blockNumber = data.blockNumber,
        errorCode = data.errorCode,
        errorMessage = data.errorMessage,
        payload = data.payload,
    )
}
