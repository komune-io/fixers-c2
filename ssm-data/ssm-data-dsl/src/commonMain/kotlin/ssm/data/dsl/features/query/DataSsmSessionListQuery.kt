package ssm.data.dsl.features.query

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.data.dsl.model.DataSsmSession
import ssm.data.dsl.model.DataSsmSessionDTO

/**
 * Retrieve a list of all known sessions of a given SSM
 * @d2 function
 * @parent [ssm.data.dsl.SsmDataD2Query]
 * @order 20
 * @title List Sessions
 */
typealias DataSsmSessionListQueryFunction = F2Function<DataSsmSessionListQueryDTO, DataSsmSessionListQueryResultDTO>

expect interface DataSsmSessionListQueryDTO : DataQueryDTO

/**
 * @d2 query
 * @parent [DataSsmSessionListQueryFunction]
 * @title List Sessions: Parameters
 */
@Serializable
@JsExport
@JsName("DataSsmSessionListQuery")
class DataSsmSessionListQuery(
	override val ssm: SsmUri,
) : DataSsmSessionListQueryDTO

expect interface DataSsmSessionListQueryResultDTO {
	/**
	 * List of all the retrieved sessions
	 */
	val items: List<DataSsmSessionDTO>
}

/**
 * @d2 event
 * @parent [DataSsmSessionListQueryFunction]
 * @title List Sessions: Result
 */
@Serializable
@JsExport
@JsName("DataSsmSessionListQueryResult")
class DataSsmSessionListQueryResult(
	override val items: List<DataSsmSession>,
) : DataSsmSessionListQueryResultDTO
