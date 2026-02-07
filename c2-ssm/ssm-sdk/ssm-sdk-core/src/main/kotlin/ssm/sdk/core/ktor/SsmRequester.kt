package ssm.sdk.core.ktor

import org.slf4j.LoggerFactory
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.sdk.core.invoke.builder.HasGet
import ssm.sdk.core.invoke.builder.HasList
import ssm.sdk.dsl.InvokeCommandArgs
import ssm.sdk.dsl.InvokeError
import ssm.sdk.dsl.InvokeException
import ssm.sdk.dsl.InvokeReturn
import ssm.sdk.dsl.InvokeType
import ssm.sdk.dsl.SsmCmdSigned
import ssm.sdk.dsl.buildArgs
import ssm.sdk.dsl.buildCommandArgs
import ssm.sdk.json.JSONConverter
import ssm.sdk.json.JsonUtils
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.exc.MismatchedInputException

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
			args.fcn,
			args.args
		)
		val request = coopRepository.query(
			cmd = InvokeType.QUERY.value,
			fcn = args.fcn,
			args = args.args,
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
			cmd = InvokeType.QUERY.value,
			fcn = args.fcn,
			args = args.args,
			channelId = chaincodeUri.channelId,
			chaincodeId = chaincodeUri.chaincodeId,
		)
		logger.info(
			"Query the blockchain in chaincode[{}] with fcn[{}] with args:{}",
			chaincodeUri.uri,
			args.fcn,
			args.args
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

	suspend fun <T> query(queries: List<SsmApiQuery>, type: TypeReference<List<T>>): List<T> {
		val total = queries.size
		queries.logger("Query", total, { it.chaincodeUri })
		val args = queries.mapIndexed { index, query ->
			val args = query.query.queryArgs(query.value)
			val invokeArgs = InvokeCommandArgs(
				cmd = InvokeType.QUERY,
				chaincodeUri = query.chaincodeUri,
				fcn = args.fcn,
				args = args.args
			)
			logger.debug(
				"Invoke[${index+1}/$total] the blockchain in channel[{}:{}] with command[{}] with args:{}",
				query.chaincodeUri.channelId,
				query.chaincodeUri.chaincodeId,
				invokeArgs.fcn,
				invokeArgs,
			)
			invokeArgs
		}
		return coopRepository.invoke(
			args
		).handleResponse {
			JsonUtils.toObject(it, type)
		}
	}

	suspend fun <T> list(chaincodeUri: ChaincodeUri, query: HasList, clazz: Class<T>): List<T> {
		val args = query.listArgs()
		val request = coopRepository.query(
			cmd = InvokeType.QUERY.value,
			fcn = args.fcn,
			args = args.args,
			channelId = chaincodeUri.channelId,
			chaincodeId = chaincodeUri.chaincodeId,
		)
		logger.info(
			"Query the blockchain in chaincode[${chaincodeUri.uri}] with fcn[${args.fcn}] with args:${args.args}",
		)
		return request.handleResponse { response ->
			jsonConverter.toCompletableObjects(clazz, response)
		}
	}

	@Throws(Exception::class)
	suspend operator fun invoke(cmdSigned: SsmCmdSigned): InvokeReturn {
		val invokeArgs = cmdSigned.buildArgs()
		logger.info(
			"Invoke[single] the blockchain in channel[{}]  with command[{}] with args:{}",
			cmdSigned.chaincodeUri.chaincodeId,
			invokeArgs.fcn,
			invokeArgs
		)
		return coopRepository.invoke(
			cmd = InvokeType.INVOKE,
			fcn = invokeArgs.fcn,
			args = invokeArgs.args,
			channelId = cmdSigned.chaincodeUri.channelId,
			chaincodeId = cmdSigned.chaincodeUri.chaincodeId,
		).handleResponse {
			jsonConverter.toCompletableObject(InvokeReturn::class.java, it)!!
		}
	}

	@Throws(Exception::class)
	suspend operator fun invoke(cmds: List<SsmCmdSigned>): List<InvokeReturn> {
		val total = cmds.size
		cmds.logger("Invoke", total, { it.chaincodeUri })

		val args = cmds.mapIndexed { index, cmd ->
			val invokeArgs = cmd.buildCommandArgs(InvokeType.INVOKE)
			logger.debug(
				"Invoke[${index+1}/$total] the blockchain in channel[{}:{}] with command[{}] with args:{}",
				cmd.chaincodeUri.channelId,
				cmd.chaincodeUri.chaincodeId,
				invokeArgs.fcn,
				invokeArgs,
			)
			invokeArgs
		}

		return coopRepository.invoke(
			args
		).handleResponse {
			JsonUtils.toObject<List<InvokeReturn>>(it)
		}
	}

	@Suppress("TooGenericExceptionCaught", "SwallowedException")
	private fun <R> String.handleResponse(transform: (String)-> R): R = try {
		transform(this)
	} catch (e: MismatchedInputException) {
		if (this.isBlank()) {
			throw InvokeException("Empty response from blockchain")
		}
		val error: InvokeError = JsonUtils.toObject(this)
		throw InvokeException(error.message)
	} catch (e: Exception) {
		throw InvokeException("Error while parsing response", e)
	}
}

data class SsmApiQuery(
	val chaincodeUri: ChaincodeUri,
	val value: String,
	val query: HasGet,
)
