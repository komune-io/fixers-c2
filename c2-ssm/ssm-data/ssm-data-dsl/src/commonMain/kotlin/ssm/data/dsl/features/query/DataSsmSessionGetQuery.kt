package ssm.data.dsl.features.query

import f2.dsl.cqrs.Event
import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.SsmUriDTO
import ssm.data.dsl.model.DataSsmSession
import ssm.data.dsl.model.DataSsmSessionDTO

/**
 * Retrieves a given session.
 * @d2 function
 * @parent [ssm.data.dsl.DataSsmD2Query]
 * @order 10
 * @title Get Session
 */
typealias DataSsmSessionGetQueryFunction = F2Function<DataSsmSessionGetQueryDTO, DataSsmSessionGetQueryResultDTO>

@JsExport
@JsName("DataSsmSessionGetQueryDTO")
interface DataSsmSessionGetQueryDTO : DataQueryDTO {
    /**
     * Identifier of the session to retrieve.
     * @example [DataSsmSession.sessionName]
     */
    val sessionName: SessionName
    override val ssmUri: SsmUriDTO
}

/**
 * @d2 query
 * @parent [DataSsmSessionGetQueryFunction]
 * @title Get Session: Parameters
 */
@Serializable
@JsExport
@JsName("DataSsmSessionGetQuery")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataSsmSessionGetQuery(
    override val sessionName: SessionName,
    override val ssmUri: SsmUri,
) : DataSsmSessionGetQueryDTO

@JsExport
@JsName("DataSsmSessionGetQueryResultDTO")
interface DataSsmSessionGetQueryResultDTO: Event {
    /**
     * The retrieved session if it exists.
     */
    val item: DataSsmSessionDTO?
}

/**
 * @d2 event
 * @parent [DataSsmSessionGetQueryFunction]
 * @title Get Session: Result
 */
@Serializable
@JsExport
@JsName("DataSsmSessionGetQueryResult")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class DataSsmSessionGetQueryResult(
    override val item: DataSsmSession?,
) : DataSsmSessionGetQueryResultDTO
