package ssm.chaincode.dsl.query

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.SsmItemResultDTO
import ssm.chaincode.dsl.SsmQueryDTO
import ssm.chaincode.dsl.model.Agent

/**
 * Retrieves an admin
 * @d2 function
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Query]
 * @title Get User
 * @order 10
 */
typealias SsmGetUserFunction = F2Function<SsmGetUserQuery, SsmGetUserResult>

/**
 * @d2 query
 * @parent [SsmGetUserFunction]
 * @title Get User: Parameters
 */
@Serializable
@JsExport
@JsName("SsmGetUserQuery")
class SsmGetUserQuery(
    override val chaincodeUri: ChaincodeUri,
    /**
	 * The name of the user.
	 * @example ["Chuck"]
	 */
	val name: String,
) : SsmQueryDTO

/**
 * @d2 event
 * @parent [SsmGetUserFunction]
 * @title Get User: Result
 */
@Serializable
@JsExport
@JsName("SsmGetUserResult")
class SsmGetUserResult(
	/**
	 * The user found.
	 */
	override val item: Agent?,
) : SsmItemResultDTO<Agent>
