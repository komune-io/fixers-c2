package ssm.data.dsl.features.query

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.blockchain.TransactionId
import ssm.data.dsl.model.DataSsmSessionId
import ssm.data.dsl.model.DataSsmSessionState
import ssm.data.dsl.model.DataSsmSessionStateDTO

/**
 * Retrieve the state of a session corresponding to a given transaction within the blockchain
 * @d2 function
 * @parent [ssm.data.dsl.model.DataSsmSession]
 * @order 30
 * @title Get Session Log
 */
typealias DataSsmSessionLogGetQueryFunction = F2Function<DataSsmSessionLogGetQueryDTO, DataSsmSessionLogGetQueryResultDTO>

expect interface DataSsmSessionLogGetQueryDTO : DataQueryDTO {
	/**
	 * Identifier of the session to retrieve
	 * @example [ssm.data.dsl.model.DataSsmSession.id]
	 */
	val sessionId: DataSsmSessionId

	/**
	 * Identifier of the transaction to retrieve
	 * @example [ssm.chaincode.dsl.blockchain.Transaction.transactionId]
	 */
	val txId: TransactionId
	override val ssm: SsmName
	override val bearerToken: String?
}

/**
 * @d2 query
 * @parent [DataSsmSessionLogGetQueryFunction]
 * @title Get Session Log: Parameters
 */
@Serializable
@JsExport
@JsName("DataSsmSessionLogGetQuery")
class DataSsmSessionLogGetQuery(
	override val sessionId: DataSsmSessionId,
	override val txId: TransactionId,
	override val ssm: SsmName,
	override val bearerToken: String?,
) : DataSsmSessionLogGetQueryDTO

expect interface DataSsmSessionLogGetQueryResultDTO {
	/**
	 * The retrieved session state and transaction
	 */
	val ssmSessionState: DataSsmSessionStateDTO?
}

/**
 * @d2 event
 * @parent [DataSsmSessionLogGetQueryFunction]
 * @title Get Session Log: Result
 */
@Serializable
@JsExport
@JsName("DataSsmSessionLogGetQueryResult")
class DataSsmSessionLogGetQueryResult(
	override val ssmSessionState: DataSsmSessionState?,
) : DataSsmSessionLogGetQueryResultDTO
