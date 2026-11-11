package ssm.data.dsl.features.query

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.SsmUriDTO
import ssm.data.dsl.model.DataSsmSessionState

/**
 * Retrieve all the logs of a given SSM session.
 * @d2 function
 * @parent [ssm.data.dsl.DataSsmD2Query]
 * @order 40
 * @title List Session Logs
 */
typealias DataSsmSessionLogListQueryFunction
        = F2Function<DataSsmSessionLogListQueryDTO, DataSsmSessionLogListQueryResultDTO>

@JsExport
@JsName("DataSsmSessionLogListQueryDTO")
interface DataSsmSessionLogListQueryDTO : DataQueryDTO {
    /**
     * Identifier of the session to retrieve.
     * @example "DataSsmSessionId"
     */
    val sessionName: SessionName
    override val ssmUri: SsmUriDTO
}

/**
 * @d2 query
 * @parent [DataSsmSessionLogListQueryFunction]
 * @title List Session Logs: Parameters
 */
@Serializable
@JsExport
@JsName("DataSsmSessionLogListQuery")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataSsmSessionLogListQuery(
    override val sessionName: SessionName,
    override val ssmUri: SsmUri,
) : DataSsmSessionLogListQueryDTO

@JsExport
@JsName("DataSsmSessionLogListQueryResultDTO")
interface DataSsmSessionLogListQueryResultDTO {
    /**
     * All retrieved logs of the given session.
     */
    val items: List<DataSsmSessionState>
}

/**
 * @d2 event
 * @parent [DataSsmSessionLogListQueryFunction]
 * @title List Session Logs: Result
 */
@Serializable
@JsExport
@JsName("SsmSessionLogListQueryResult")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataSsmSessionLogListQueryResult(
    override val items: List<DataSsmSessionState>,
) : DataSsmSessionLogListQueryResultDTO
