package ssm.data.dsl.features.query

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.data.dsl.model.DataSsmSession
import ssm.data.dsl.model.DataSsmSessionDTO

/**
 * Retrieve a list of all known sessions of a given SSM.
 * @d2 function
 * @parent [ssm.data.dsl.DataSsmD2Query]
 * @order 20
 * @title List Sessions
 */
typealias DataSsmSessionListQueryFunction = F2Function<DataSsmSessionListQueryDTO, DataSsmSessionListQueryResultDTO>

@JsExport
@JsName("DataSsmSessionListQueryDTO")
interface DataSsmSessionListQueryDTO : DataQueryDTO

/**
 * @d2 query
 * @parent [DataSsmSessionListQueryFunction]
 * @title List Sessions: Parameters
 */
@Serializable
@JsExport
@JsName("DataSsmSessionListQuery")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataSsmSessionListQuery(
    override val ssmUri: SsmUri,
) : DataSsmSessionListQueryDTO

@JsExport
@JsName("DataSsmSessionListQueryResultDTO")
interface DataSsmSessionListQueryResultDTO {
    /**
     * List of all the retrieved sessions.
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
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataSsmSessionListQueryResult(
    override val items: List<DataSsmSession>,
) : DataSsmSessionListQueryResultDTO
