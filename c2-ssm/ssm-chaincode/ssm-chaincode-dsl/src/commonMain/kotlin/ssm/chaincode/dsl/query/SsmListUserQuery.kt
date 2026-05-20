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
 * Retrieves all users
 * @d2 function
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Query]
 * @title List Users
 * @order 30
 */
typealias SsmListUserQueryFunction = F2Function<SsmListUserQuery, SsmListUserResult>

/**
 * @d2 query
 * @parent [SsmListUserQueryFunction]
 * @title List Users: Parameters
 */
@Serializable
@JsExport
@JsName("SsmListUserQuery")
class SsmListUserQuery(
	override val chaincodeUri: ChaincodeUri
) : SsmQueryDTO

/**
 * @d2 event
 * @parent [SsmListUserQueryFunction]
 * @title List Users: Result
 */
@Serializable
@JsExport
@JsName("SsmListUserResult")
class SsmListUserResult(
	override val items: Array<AgentName>,
) : SsmItemsResultDTO<AgentName>
