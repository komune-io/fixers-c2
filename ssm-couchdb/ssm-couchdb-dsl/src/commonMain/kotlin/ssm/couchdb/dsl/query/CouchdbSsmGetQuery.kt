package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmDTO
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.uri.SsmUri

/**
 * Retrieves a ssm stored in the database.
 * @title Get SSM
 * @d2 function
 * @parent [ssm.couchdb.dsl.SsmCouchdbD2Query]
 *
 */
typealias CouchdbSsmGetQueryFunction = F2Function<CouchdbSsmGetQuery, CouchdbSsmGetQueryResult>

/**
 * @d2 query
 * @parent [CouchdbSsmGetQueryFunction]
 * @title Parameters
 */
expect interface CouchdbSsmGetQueryDTO : Query {
	/**
	 * The unique id of a channel.
	 */
	val channelId: ChannelId
	/**
	 * The unique id of a chaincode.
	 */
	val chaincodeId: ChaincodeId

	/**
	 * The name of ssm
	 * @example "ssmsmartb"
	 */
	val ssmName: SsmName
}

/**
 * @d2 event
 * @parent [CouchdbSsmGetQueryFunction]
 * @order 30
 * @title Result
 */
expect interface CouchdbSsmGetQueryResultDTO: Event {
	val uri: SsmUri
	val item: SsmDTO?
}

@Serializable
@JsExport
@JsName("CouchdbSsmGetQuery")
class CouchdbSsmGetQuery(
	override val channelId: ChannelId,
	override val chaincodeId: ChaincodeId,
	override val ssmName: SsmName
) : CouchdbSsmGetQueryDTO

@Serializable
@JsExport
@JsName("CouchdbSsmGetQueryResult")
class CouchdbSsmGetQueryResult(
	override val uri: SsmUri,
	override val item: Ssm?
) : CouchdbSsmGetQueryResultDTO
