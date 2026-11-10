package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName
import ssm.couchdb.dsl.model.ChangeEventId
import ssm.couchdb.dsl.model.DatabaseChanges
import ssm.couchdb.dsl.model.DatabaseChangesDTO

/**
 * @title Retrieves all changes on a database
 * @d2 function
 * @order 20
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Query]
 */
typealias CouchdbDatabaseGetChangesQueryFunction
        = F2Function<CouchdbDatabaseGetChangesQueryDTO, CouchdbDatabaseGetChangesQueryResultDTO>

/**
 * @title Get Database: Parameters
 * @d2 model
 * @parent [CouchdbDatabaseGetQueryFunction]
 */
@JsExport
@JsName("CouchdbDatabaseGetChangesQueryDTO")
interface CouchdbDatabaseGetChangesQueryDTO : Query {
    /**
     * The unique id of a channel.
     */
    val channelId: ChannelId
    /**
     * The unique id of a chaincode.
     */
    val chaincodeId: ChaincodeId
    /**
     * The name of a ssm.
     */
    val ssmName: SsmName
    /**
     * The name of a session.
     */
    val sessionName: SessionName?
    /**
     * Filter to start the results from the the ID of the last events received by the server on a previous connection
     */
    val lastEventId: ChangeEventId?
    /**
     * Filter to start the results from the the ID of the last events received by the server on a previous connection
     */
    val limit: Long?
}

/**
 * @d2 model
 * @title Get Database: Result
 * @parent [CouchdbDatabaseGetQueryFunction]
 */
@JsExport
@JsName("CouchdbDatabaseGetChangesQueryResultDTO")
interface CouchdbDatabaseGetChangesQueryResultDTO : Event {
    /**
     * The name of the database.
     */
    val items: List<DatabaseChangesDTO>
    val lastEventId: ChangeEventId?
}

@Serializable
@JsExport
@JsName("CouchdbDatabaseGetChangesQuery")
class CouchdbDatabaseGetChangesQuery(
    override val channelId: ChannelId,
    override val chaincodeId: ChaincodeId,
    override val lastEventId: ChangeEventId?,
    override val ssmName: SsmName,
    override val sessionName: SessionName?,
    override val limit: Long?,
) : CouchdbDatabaseGetChangesQueryDTO

@JsExport
@JsName("CouchdbDatabaseGetChangesQueryResult")
class CouchdbDatabaseGetChangesQueryResult(
    override val items: List<DatabaseChanges>,
    override val lastEventId: ChangeEventId?,
) : CouchdbDatabaseGetChangesQueryResultDTO
