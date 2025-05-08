package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentDTO

/**
 * @title Fetch all admins
 * @d2 function
 * @order 20
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Query]
 */
typealias CouchdbUserListQueryFunction = F2Function<CouchdbUserListQueryDTO, CouchdbUserListQueryResultDTO>

/**
 * @title Get all chaincode: Parameters
 * @d2 model
 * @parent [CouchdbUserListQueryFunction]
 */

@JsExport
@JsName("CouchdbUserListQueryDTO")
interface CouchdbUserListQueryDTO : Query {
	/**
	 * The unique id of a chaincode.
	 */
	val chaincodeUri: ChaincodeUriDTO
}

/**
 * @d2 model
 * @title Get all admins: Result
 * @parent [CouchdbUserListQueryFunction]
 */
@JsExport
@JsName("CouchdbUserListQueryResultDTO")
interface CouchdbUserListQueryResultDTO : Event {
	/**
	 * Names of the admin.
	 */
	val items: List<AgentDTO>
}

@Serializable
@JsExport
@JsName("CouchdbUserListQuery")
class CouchdbUserListQuery(
	override val chaincodeUri: ChaincodeUri
) : CouchdbUserListQueryDTO

@Serializable
@JsExport
@JsName("CouchdbUserListQueryResult")
class CouchdbUserListQueryResult(
	override val items: List<Agent>,
) : CouchdbUserListQueryResultDTO
