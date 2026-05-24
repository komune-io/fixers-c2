package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable

/**
 * @title Fetch all chaincodes
 * @d2 function
 * @order 20
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Query]
 */
typealias CouchdbChaincodeListQueryFunction
		= F2Function<CouchdbChaincodeListQueryDTO, CouchdbChaincodeListQueryResultDTO>

/**
 * @title Get all chaincode: Parameters
 * @d2 model
 * @parent [CouchdbChaincodeListQueryFunction]
 */
@JsExport
@JsName("CouchdbChaincodeListQueryDTO")
interface CouchdbChaincodeListQueryDTO : Query

/**
 * @d2 model
 * @title Get all chaincodes: Result
 * @parent [CouchdbChaincodeListQueryFunction]
 */
@JsExport
@JsName("CouchdbChaincodeListQueryResultDTO")
interface CouchdbChaincodeListQueryResultDTO : Event {
	/**
	 * The name of the database.
	 */
	val items: List<ChaincodeUriDTO>
}

@Serializable
@JsExport
@JsName("CouchdbChaincodeListQuery")
class CouchdbChaincodeListQuery: CouchdbChaincodeListQueryDTO

@Serializable
@JsExport
@JsName("CouchdbChaincodeListQueryResult")
class CouchdbChaincodeListQueryResult(
    override val items: List<ChaincodeUri>,
) : CouchdbChaincodeListQueryResultDTO
