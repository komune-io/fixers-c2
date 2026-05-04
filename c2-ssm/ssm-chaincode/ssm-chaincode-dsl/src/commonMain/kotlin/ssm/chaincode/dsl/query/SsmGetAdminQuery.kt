package ssm.chaincode.dsl.query

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.SsmItemResultDTO
import ssm.chaincode.dsl.SsmQueryDTO
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName

/**
 * Retrieves an admin
 * @d2 function
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Query]
 * @title Get Admin
 * @order 10
 */
typealias SsmGetAdminFunction = F2Function<SsmGetAdminQuery, SsmGetAdminResult>

/**
 * @d2 query
 * @parent [SsmGetAdminFunction]
 * @title Get Admin: Parameters
 */
@JsExport
@Serializable
@JsName("SsmGetAdminQuery")
class SsmGetAdminQuery(
    override val chaincodeUri: ChaincodeUri,
    /**
	 * The name of the admin.
	 * @example ["Chuck"]
	 */
	val name: AgentName,
) : SsmQueryDTO

/**
 * @d2 event
 * @parent [SsmGetAdminFunction]
 * @title Get Admin: Result
 */
@JsExport
@Serializable
@JsName("SsmGetAdminResult")
class SsmGetAdminResult(
	/**
	 * The admin found.
	 */
	override val item: Agent?,
) : SsmItemResultDTO<Agent>
