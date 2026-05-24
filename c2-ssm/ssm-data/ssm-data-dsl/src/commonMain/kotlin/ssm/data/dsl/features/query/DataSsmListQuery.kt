package ssm.data.dsl.features.query

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.data.dsl.model.DataSsm
import ssm.data.dsl.model.DataSsmDTO

/**
 * Retrieves all known SSMs
 * @d2 function
 * @parent [ssm.data.dsl.DataSsmD2Query]
 * @order 20
 * @title List SSMs
 */
typealias DataSsmListQueryFunction = F2Function<DataSsmListQuery, DataSsmListQueryResult>

@JsExport
@JsName("DataSsmListQueryDTO")
interface DataSsmListQueryDTO

/**
 * @d2 query
 * @parent [DataSsmListQueryFunction]
 * @title List SSMs: Parameters
 */
@Serializable
@JsExport
@JsName("DataSsmListQuery")
class DataSsmListQuery(
	val chaincodes: List<ChaincodeUri>
) : DataSsmListQueryDTO

@JsExport
@JsName("DataSsmListQueryResultDTO")
interface DataSsmListQueryResultDTO {
	/**
	 * List of all retrieved SSMs
	 */
	val items: List<DataSsmDTO>
}

/**
 * @d2 event
 * @parent [DataSsmListQueryFunction]
 * @title List SSMs: Result
 */
@Serializable
@JsExport
@JsName("DataSsmListQueryResult")
class DataSsmListQueryResult(
	override val items: List<DataSsm>,
) : DataSsmListQueryResultDTO
