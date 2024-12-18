package ssm.sdk.core.ktor

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
import io.ktor.http.path
import io.ktor.serialization.jackson.jackson
import org.slf4j.LoggerFactory
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.from
import ssm.sdk.core.auth.AuthCredentials
import ssm.sdk.core.auth.BearerTokenAuthCredentials
import ssm.sdk.dsl.InvokeCommandArgs
import ssm.sdk.dsl.InvokeType


class KtorRepository(
	private val baseUrl: String,
	private val timeout: Long,

	private val authCredentials: AuthCredentials?,
) {
	private val logger = LoggerFactory.getLogger(javaClass)
	companion object {
		const val PATH = "/"
		const val CMD_PROPS = "cmd"
		const val CHANNEL_ID_PROPS = "channelid"
		const val CHAINCODE_ID_PROPS = "chaincodeid"
		const val FCN_PROPS = "fcn"
		const val ARGS_PROPS = "args"
	}

	val client = HttpClient(CIO) {
		if(logger.isDebugEnabled) {
			install(Logging)
		}
		install(ContentNegotiation) {
			jackson()
		}
		install(HttpTimeout) {
			requestTimeoutMillis = timeout
		}
	}

	suspend fun query(
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

	suspend fun invoke(
		cmd: InvokeType,
		fcn: String,
		args: List<String>,
		channelId: ChannelId?,
		chaincodeId: ChaincodeId?,
	): String {
		return invoke(InvokeCommandArgs(
			chaincodeUri= ChaincodeUri.from(channelId = channelId, chaincodeId = chaincodeId),
			cmd = cmd,
			args = args,
			fcn = fcn
		))
	}

	suspend fun invoke(
		invokeArgs: InvokeCommandArgs
	): String {
		val body = mapOf(
			CMD_PROPS to invokeArgs.cmd.value,
			FCN_PROPS to invokeArgs.fcn,
			ARGS_PROPS to invokeArgs.args,
			CHANNEL_ID_PROPS to invokeArgs.chaincodeUri?.channelId,
			CHAINCODE_ID_PROPS to invokeArgs.chaincodeUri?.chaincodeId,
		)
		return client.post(baseUrl) {
			addAuth()
			contentType(ContentType.Application.Json)
			setBody(body)
		}.bodyAsText()
	}

	suspend fun invoke(
		invokeArgs: List<InvokeCommandArgs>
	): String {
		val body = invokeArgs.map { invokeArg ->
			mapOf(
				CMD_PROPS to invokeArg.cmd.value,
				FCN_PROPS to invokeArg.fcn,
				ARGS_PROPS to invokeArg.args,
				CHANNEL_ID_PROPS to invokeArg.chaincodeUri?.channelId,
				CHAINCODE_ID_PROPS to invokeArg.chaincodeUri?.chaincodeId,
			)
		}
		return client.post("$baseUrl/invoke") {
			addAuth()
			contentType(ContentType.Application.Json)
			setBody(body)
		}.bodyAsText()
	}

	suspend fun invokeF2(
		invokeArgs: List<InvokeCommandArgs>
	): String {
		val body = invokeArgs.map { invokeArg ->
			mapOf(
				CMD_PROPS to invokeArg.cmd.value,
				FCN_PROPS to invokeArg.fcn,
				ARGS_PROPS to invokeArg.args,
				CHANNEL_ID_PROPS to invokeArg.chaincodeUri?.channelId,
				CHAINCODE_ID_PROPS to invokeArg.chaincodeUri?.chaincodeId,
			)
		}
		return client.post("$baseUrl/invokeF2") {
			addAuth()
			contentType(ContentType.Application.Json)
			setBody(body)
		}.bodyAsText()
	}

	private fun HttpRequestBuilder.addAuth() {
		when (authCredentials) {
			is BearerTokenAuthCredentials -> header("Authorization", "Bearer ${authCredentials.getBearerToken()}")
			else -> return
		}
	}

}
