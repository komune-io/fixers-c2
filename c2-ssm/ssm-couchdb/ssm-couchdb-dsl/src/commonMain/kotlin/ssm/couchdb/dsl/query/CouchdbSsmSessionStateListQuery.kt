package ssm.couchdb.dsl.query

import f2.dsl.cqrs.page.OffsetPaginationDTO
import f2.dsl.cqrs.page.PageQueryDTO
import f2.dsl.cqrs.page.PageQueryResultDTO
import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.SsmSessionStateDTO

/**
 * Retrieve the list of all known sessions of a given SSM
 * @d2 function
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Query]
 * @order 40
 * @title List Sessions
 */
typealias CouchdbSsmSessionStateListQueryFunction
		= F2Function<CouchdbSsmSessionStateListQueryDTO, CouchdbSsmSessionStateListQueryResultDTO>

@JsExport
@JsName("CouchdbSsmSessionStateListQueryDTO")
interface CouchdbSsmSessionStateListQueryDTO : PageQueryDTO {
	/**
	 * The unique uri of a channel.
	 */
	val chaincodeUri: ChaincodeUriDTO
	/**
	 * The unique identifier of a ssm.
	 */
	val ssm: SsmName?
}

@JsExport
@JsName("CouchdbSsmSessionStateListQueryResultDTO")
interface CouchdbSsmSessionStateListQueryResultDTO : PageQueryResultDTO<SsmSessionStateDTO> {
	/**
	 * Retrieved sessions
	 */
	override val pagination: OffsetPaginationDTO?
	/**
	 * Retrieved sessions
	 */
	override val items: List<SsmSessionStateDTO>
}

/**
 * @d2 query
 * @parent [CouchdbSsmSessionStateListQueryFunction]
 * @title List Sessions: Parameters
 */
@Serializable
@JsExport
@JsName("CouchdbSsmSessionStateListQuery")
class CouchdbSsmSessionStateListQuery(
    override val pagination: OffsetPaginationDTO? = null,
    override val chaincodeUri: ChaincodeUri,
    override val ssm: SsmName? = null
) : CouchdbSsmSessionStateListQueryDTO

/**
 * @d2 event
 * @parent [CouchdbSsmSessionStateListQueryFunction]
 * @title List Sessions: Result
 */
@Serializable
@JsExport
@JsName("CouchdbSsmSessionStateListQueryResult")
class CouchdbSsmSessionStateListQueryResult(
	override val pagination: OffsetPaginationDTO?,
	override val total: Int,
	override val items: List<SsmSessionStateDTO>,
) : CouchdbSsmSessionStateListQueryResultDTO
