package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.chaincode.dsl.model.SsmAgent
import ssm.chaincode.dsl.model.SsmAgentDTO
import ssm.chaincode.dsl.model.uri.ChaincodeUri

/**
 * @title Fetch all admins
 * @d2 function
 * @order 20
 * @parent [ssm.couchdb.dsl.SsmCouchdbD2Query]
 */
typealias CouchdbAdminListQueryFunction = F2Function<CouchdbAdminListQueryDTO, CouchdbAdminListQueryResultDTO>

/**
 * @title Get all chaincode: Parameters
 * @d2 model
 * @parent [CouchdbAdminListQueryFunction]
 */
expect interface CouchdbAdminListQueryDTO : Query {
	/**
	 * The unique id of a chaincode.
	 */
	val chaincodeUri: ChaincodeUri
}

/**
 * @d2 model
 * @title Get all admins: Result
 * @parent [CouchdbAdminListQueryFunction]
 */
expect interface CouchdbAdminListQueryResultDTO : Event {
	/**
	 * Names of the admin.
	 */
	val items: List<SsmAgentDTO>
}

@Serializable
@JsExport
@JsName("CouchdbAdminListQuery")
class CouchdbAdminListQuery(
	override val chaincodeUri: ChaincodeUri
) : CouchdbAdminListQueryDTO

@Serializable
@JsExport
@JsName("CouchdbAdminListQueryResult")
class CouchdbAdminListQueryResult(
	override val items: List<SsmAgent>,
) : CouchdbAdminListQueryResultDTO
