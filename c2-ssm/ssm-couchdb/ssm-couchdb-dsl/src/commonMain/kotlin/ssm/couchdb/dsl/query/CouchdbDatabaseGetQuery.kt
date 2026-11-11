package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.couchdb.dsl.model.Database
import ssm.couchdb.dsl.model.DatabaseDTO

/**
 * @title Retrieves an information about a database.
 * @d2 function
 * @order 20
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Query]
 */
typealias CouchdbDatabaseGetQueryFunction = F2Function<CouchdbDatabaseGetQueryDTO, CouchdbDatabaseGetQueryResultDTO>

/**
 * @title Get Database: Parameters
 * @d2 model
 * @parent [CouchdbDatabaseGetQueryFunction]
 */
@JsExport
@JsName("CouchdbDatabaseGetQueryDTO")
interface CouchdbDatabaseGetQueryDTO : Query {
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
 * @d2 model
 * @title Get Database: Result
 * @parent [CouchdbDatabaseGetQueryFunction]
 */
@JsExport
@JsName("CouchdbDatabaseGetQueryResultDTO")
interface CouchdbDatabaseGetQueryResultDTO : Event {
    /**
     * The name of the database.
     */
    val item: DatabaseDTO?
}

@Serializable
@JsExport
@JsName("CouchdbDatabaseGetQuery")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class CouchdbDatabaseGetQuery(
    override val channelId: ChannelId,
    override val chaincodeId: ChaincodeId,
) : CouchdbDatabaseGetQueryDTO

@Serializable
@JsExport
@JsName("CouchdbDatabaseGetQueryResult")
// @JsExport: a data class would add copy and componentN to the JS surface.
@Suppress("UseDataClass")
class CouchdbDatabaseGetQueryResult(
    override val item: Database?,
) : CouchdbDatabaseGetQueryResultDTO
