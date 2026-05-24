package ssm.data.dsl.features.query

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.TransactionId
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.SsmUriDTO
import ssm.data.dsl.model.DataSsmSessionState
import ssm.data.dsl.model.DataSsmSessionStateDTO

/**
 * Retrieve the state of a session corresponding to a given transaction within the blockchain
 * @d2 function
 * @parent [ssm.data.dsl.model.DataSsmSession]
 * @order 30
 * @title Get Session Log
 */
typealias DataSsmSessionLogGetQueryFunction
		= F2Function<DataSsmSessionLogGetQueryDTO, DataSsmSessionLogGetQueryResultDTO>

@JsExport
@JsName("DataSsmSessionLogGetQueryDTO")
interface DataSsmSessionLogGetQueryDTO : DataQueryDTO {
	/**
	 * Identifier of the session to retrieve
	 * @example [ssm.data.dsl.model.DataSsmSession.sessionName]
	 */
	val sessionName: SessionName

	/**
	 * Identifier of the transaction to retrieve
	 * @example [ssm.chaincode.dsl.blockchain.Transaction.transactionId]
	 */
	val txId: TransactionId
	override val ssmUri: SsmUriDTO
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
    override val sessionName: SessionName,
    override val txId: TransactionId,
    override val ssmUri: SsmUri,
) : DataSsmSessionLogGetQueryDTO

@JsExport
@JsName("DataSsmSessionLogGetQueryResultDTO")
interface DataSsmSessionLogGetQueryResultDTO {
	/**
	 * The retrieved session state and transaction
	 */
	val item: DataSsmSessionStateDTO?
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
	override val item: DataSsmSessionState?,
) : DataSsmSessionLogGetQueryResultDTO
