package ssm.couchdb.dsl.query

import f2.dsl.cqrs.page.OffsetPaginationDTO
import f2.dsl.cqrs.page.PageQueryDTO
import f2.dsl.cqrs.page.PageQueryResultDTO
import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmDTO

/**
 * Retrieves the list of ssm stored in the database.
 * @title List SSMs
 * @d2 function
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Query]
 *
 */
typealias CouchdbSsmListQueryFunction = F2Function<CouchdbSsmListQuery, CouchdbSsmListQueryResult>

/**
 * @d2 query
 * @parent [CouchdbSsmListQueryFunction]
 * @title Parameters
 */
@JsExport
@JsName("CouchdbSsmListQueryDTO")
interface CouchdbSsmListQueryDTO : PageQueryDTO {
    /**
     * The unique id of a channel.
     */
    val channelId: ChannelId
    /**
     * The unique id of a chaincode.
     */
    val chaincodeId: ChaincodeId
}

/**
 * @d2 event
 * @parent [CouchdbSsmListQueryFunction]
 * @order 30
 * @title Result
 */
@JsExport
@JsName("CouchdbSsmListQueryResultDTO")
interface CouchdbSsmListQueryResultDTO: PageQueryResultDTO<SsmDTO> {
    override val pagination: OffsetPaginationDTO?
    override val items: List<SsmDTO>
    override val total: Int
}

@Serializable
@JsExport
@JsName("CouchdbSsmListQuery")
class CouchdbSsmListQuery(
    override val pagination: OffsetPaginationDTO?,
    override val channelId: ChannelId,
    override val chaincodeId: ChaincodeId,
) : CouchdbSsmListQueryDTO

@Serializable
@JsExport
@JsName("CouchdbSsmListQueryResult")
class CouchdbSsmListQueryResult(
    override val pagination: OffsetPaginationDTO?,
    override val items: List<Ssm>,
    override val total: Int,
) : CouchdbSsmListQueryResultDTO
