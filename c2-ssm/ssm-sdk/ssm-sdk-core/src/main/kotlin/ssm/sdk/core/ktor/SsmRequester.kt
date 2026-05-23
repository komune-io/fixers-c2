package ssm.sdk.core.ktor

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import io.komune.c2.chaincode.dsl.invoke.InvokeReturn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import ssm.sdk.core.invoke.builder.HasGet
import ssm.sdk.core.invoke.builder.HasList
import ssm.sdk.dsl.CommandOutcome
import ssm.sdk.dsl.InvokeException
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.dsl.buildCommandArgs
import ssm.sdk.json.JSONConverter
import ssm.sdk.json.JsonUtils
import tools.jackson.core.type.TypeReference
import java.util.UUID

class SsmRequester(
	private val jsonConverter: JSONConverter,
	private val coopRepository: KtorRepository,
) {

	private val logger = LoggerFactory.getLogger(SsmRequester::class.java)

	suspend fun <T> logger(
        chaincodeUri: ChaincodeUri, value: String, query: HasGet, clazz: TypeReference<List<T>>
	): List<T> {
		val args = query.queryArgs(value)
		logger.info(
			"Query[Log] the blockchain in chaincode[{}] with fcn[{}] with args:{}",
			chaincodeUri.uri,
			args.function,
			args.values
		)
		val request = coopRepository.query(
			cmd = InvokeRequestType.query.name,
			fcn = args.function,
			args = args.values,
			channelId = chaincodeUri.channelId,
			chaincodeId = chaincodeUri.chaincodeId,
		)
		return request.let {
			JsonUtils.toObject(it, clazz)
		}
	}

	suspend fun <T> query(chaincodeUri: ChaincodeUri, value: String, query: HasGet, clazz: Class<T>): T? {
		val args = query.queryArgs(value)
		val request = coopRepository.query(
			cmd = InvokeRequestType.query.name,
			fcn = args.function,
			args = args.values,
			channelId = chaincodeUri.channelId,
			chaincodeId = chaincodeUri.chaincodeId,
		)
		logger.info(
			"Query the blockchain in chaincode[{}] with fcn[{}] with args:{}",
			chaincodeUri.uri,
			args.function,
			args.values
		)
		return request.let {
			jsonConverter.toCompletableObject(clazz, it)
		}
	}

	private fun <T> List<T>.logger(type: String, total: Int, toChaincode: (T) -> ChaincodeUri) = map(toChaincode).toSet()
	.joinToString { "[${it.channelId}:${it.chaincodeId}]]" }
	.let { chaincodeUri ->
		logger.info(
			"$type[$total] the blockchain in channel[$chaincodeUri]",
		)
	}

	/**
	 * Batched query helper. Each [SsmApiQuery] is dispatched in parallel to the gateway's
	 * GET `/` query endpoint and the results are collected in input order.
	 *
	 * Replaces the prior V1 batched-query-via-POST-/invoke shape. CloudEvents-shape
	 * `/invoke` is for write transactions only; query routing stays on GET `/`.
	 */
	suspend fun <T> query(queries: List<SsmApiQuery>, type: TypeReference<List<T>>): List<T> = coroutineScope {
		val total = queries.size
		queries.logger("Query", total) { it.chaincodeUri }
		val raws: List<String> = queries.map { query ->
			async {
				val args = query.query.queryArgs(query.value)
				coopRepository.query(
					cmd = InvokeRequestType.query.name,
					fcn = args.function,
					args = args.values,
					channelId = query.chaincodeUri.channelId,
					chaincodeId = query.chaincodeUri.chaincodeId,
				)
			}
		}.awaitAll()
		raws.flatMap { raw -> raw.handleResponse { JsonUtils.toObject<List<T>>(it, type) } }
	}

	suspend fun <T> list(chaincodeUri: ChaincodeUri, query: HasList, clazz: Class<T>): List<T> {
		val args = query.listArgs()
		val request = coopRepository.query(
			cmd = InvokeRequestType.query.name,
			fcn = args.function,
			args = args.values,
			channelId = chaincodeUri.channelId,
			chaincodeId = chaincodeUri.chaincodeId,
		)
		logger.info(
			"Query the blockchain in chaincode[${chaincodeUri.uri}] with fcn[${args.function}] with args:${args.values}",
		)
		return request.handleResponse { response ->
			jsonConverter.toCompletableObjects(clazz, response)
		}
	}

	/**
	 * Single-command invoke convenience facade. Synthesizes a UUID msgId because the
	 * caller doesn't supply correlation; converts the V2 [CommandOutcome] back into
	 * the legacy [InvokeReturn] SUCCESS/ERROR shape so call sites that haven't
	 * migrated to per-item outcomes keep working.
	 */
	@Throws(Exception::class)
	suspend operator fun invoke(cmdSigned: SsmCmdSigned): InvokeReturn {
		logger.info(
			"Invoke[single] the blockchain in channel[{}] with chaincode[{}]",
			cmdSigned.chaincodeUri.channelId,
			cmdSigned.chaincodeUri.chaincodeId,
		)
		return invokeAll(listOf(cmdSigned), listOf(UUID.randomUUID().toString())).first().toInvokeReturn()
	}

	/** Batched convenience facade — same shimming as the single-arg form. */
	@Throws(Exception::class)
	suspend operator fun invoke(cmds: List<SsmCmdSigned>): List<InvokeReturn> {
		val total = cmds.size
		cmds.logger("Invoke", total) { it.chaincodeUri }
		val msgIds = cmds.map { UUID.randomUUID().toString() }
		return invokeAll(cmds, msgIds).map { it.toInvokeReturn() }
	}

	@Throws(Exception::class)
	suspend fun invokeAll(
		cmds: List<SsmCmdSigned>,
		msgIds: List<String>,
	): List<CommandOutcome> {
		require(msgIds.size == cmds.size) {
			"msgIds.size=${msgIds.size} must match cmds.size=${cmds.size}"
		}
		val total = cmds.size
		cmds.logger("Invoke[v2]", total) { it.chaincodeUri }

		val args = cmds.map { it.buildCommandArgs(InvokeRequestType.invoke) }
		return coopRepository.invoke(args, msgIds)
	}

	private fun <R> String.handleResponse(transform: (String) -> R): R = try {
		transform(this)
	} catch (e: Exception) {
		val excerpt = this.take(HANDLE_RESPONSE_EXCERPT_LIMIT).ifBlank { "<empty>" }
		throw InvokeException("Error parsing response: $excerpt", e)
	}

	companion object {
		private const val HANDLE_RESPONSE_EXCERPT_LIMIT = 200
	}
}

private fun CommandOutcome.toInvokeReturn(): InvokeReturn = if (outcome == "Committed") {
	InvokeReturn(status = "SUCCESS", info = "", transactionId = transactionId.orEmpty())
} else {
	val info = listOfNotNull(errorCode, errorMessage).joinToString(": ")
	InvokeReturn(status = "ERROR", info = info, transactionId = "")
}

data class SsmApiQuery(
    val chaincodeUri: ChaincodeUri,
    val value: String,
    val query: HasGet,
)
