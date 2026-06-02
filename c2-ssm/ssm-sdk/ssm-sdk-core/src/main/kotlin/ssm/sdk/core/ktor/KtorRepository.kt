package ssm.sdk.core.ktor

import io.komune.c2.chaincode.dsl.cloudevent.InvokeEnvelope
import io.komune.c2.chaincode.dsl.cloudevent.InvokeType
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.serialization.jackson.jackson
import org.slf4j.LoggerFactory
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.sdk.core.auth.AuthCredentials
import ssm.sdk.core.auth.BearerTokenAuthCredentials
import ssm.sdk.core.repository.SsmRequesterRepository
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.json.JsonUtils
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class KtorRepository(
	private val baseUrl: String,
	private val timeout: Long,
	private val authCredentials: AuthCredentials?,
	private val cloudEventsSource: String = DEFAULT_CLOUDEVENTS_SOURCE,
	client: HttpClient? = null,
) : SsmRequesterRepository {
	private val logger = LoggerFactory.getLogger(javaClass)
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
		cmd: String,
		fcn: String,
		args: List<String>,
		channelId: ChannelId?,
		chaincodeId: ChaincodeId?,
	): String {
		return client.get(baseUrl + PATH) {
			addAuth()
			parameter(CMD_PROPS, cmd)
			channelId?.let { parameter(CHANNEL_ID_PROPS, channelId) }
			chaincodeId?.let { parameter(CHAINCODE_ID_PROPS, chaincodeId) }
			parameter(FCN_PROPS, fcn)
			parameter(ARGS_PROPS, args.first())
		}.bodyAsText()
	}

	suspend fun getBlock(blockId: Long, channelId: ChannelId?): String {
		return client.get(baseUrl) {
			addAuth()

			channelId?.let { parameter("channelId", channelId) }
			url {
				path("blocks", blockId.toString())
			}
		}.bodyAsText()
	}

	suspend fun getTransaction(txId: String, channelId: ChannelId?): String {
		return client.get(baseUrl) {
			addAuth()
			channelId?.let { parameter("channelId", channelId) }
			url {
				path("transactions", txId)
			}
		}.bodyAsText()
	}

	/**
	 * Submits a batch of invocations to the gateway's CloudEvents-shaped `/invoke` endpoint.
	 *
	 * Producers MUST ensure `(cloudEventsSource, msgId)` pairs are unique per
	 * CloudEvents 1.0 §3.1.1 — the gateway treats `msgId` as the CE `id` attribute.
	 */
	override suspend fun invoke(
		invokeArgs: List<InvokeRequest>,
		msgIds: List<String>,
	): List<CommandOutcome> {
		require(invokeArgs.size == msgIds.size) {
			"msgIds.size=${msgIds.size} must match invokeArgs.size=${invokeArgs.size}"
		}
		val body = invokeArgs.zip(msgIds).map { (req, msgId) -> buildEnvelope(msgId, req) }
		return runCatching {
			val response = client.post("$baseUrl$INVOKE_PATH") {
				addAuth()
				contentType(ContentType.Application.Json)
				setBody(body)
			}
			mapHttpResponse(response, msgIds)
		}.getOrElse { e ->
			if (e is kotlinx.coroutines.CancellationException) throw e
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
		response: io.ktor.client.statement.HttpResponse,
		msgIds: List<String>,
	): List<CommandOutcome> {
		val statusValue = response.status.value
		return when {
			response.status.isSuccess() -> decodeOutcomes(response.bodyAsText(), msgIds)
			statusValue == HTTP_UNAUTHORIZED || statusValue == HTTP_FORBIDDEN -> synthesiseOutcomes(
				msgIds = msgIds,
				outcome = OUTCOME_REJECTED,
				errorCode = "HTTP_$statusValue",
				errorMessage = response.bodyAsText(),
			)
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
			is io.ktor.client.plugins.HttpRequestTimeoutException,
			is io.ktor.client.network.sockets.ConnectTimeoutException,
			is java.net.SocketTimeoutException -> "TIMEOUT"
			is java.net.ConnectException -> "CONNECT_REFUSED"
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
		when (authCredentials) {
			is BearerTokenAuthCredentials -> header("Authorization", "Bearer ${authCredentials.getBearerToken()}")
			else -> return
		}
	}
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val OUTCOME_REJECTED = "Rejected"
private const val OUTCOME_TRANSIENT = "Transient"
private const val OUTCOME_INDETERMINATE = "Indeterminate"

/**
 * Wire shape of the gateway's `OutcomeData` (CE `data` field).
 * Locally redeclared because the gateway type lives in the Spring app, not in a shared module.
 */
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
