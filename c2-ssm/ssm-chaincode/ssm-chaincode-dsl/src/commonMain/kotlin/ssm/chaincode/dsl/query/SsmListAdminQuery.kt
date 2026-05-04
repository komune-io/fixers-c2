package ssm.chaincode.dsl.query

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.SsmItemsResultDTO
import ssm.chaincode.dsl.SsmQueryDTO
import ssm.chaincode.dsl.model.AgentName

/**
 * Retrieves all admins
 * @d2 function
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Query]
 * @title List Admin
 * @order 30
 */
typealias SsmListAdminQueryFunction = F2Function<SsmListAdminQuery, SsmListAdminResult>

/**
 * @d2 query
 * @parent [SsmListAdminQueryFunction]
 * @title List Admins: Parameters
 */
@Serializable
@JsExport
@JsName("SsmListAdminQuery")
class SsmListAdminQuery(
	override val chaincodeUri: ChaincodeUri
) : SsmQueryDTO

/**
 * @d2 event
 * @parent [SsmListAdminQueryFunction]
 * @title List Admins: Result
 */
@Serializable
@JsExport
@JsName("SsmListAdminResult")
class SsmListAdminResult(
	override val items: Array<AgentName>,
) : SsmItemsResultDTO<AgentName>
