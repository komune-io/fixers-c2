package ssm.chaincode.dsl.query

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.SsmItemsResultDTO
import ssm.chaincode.dsl.SsmQueryDTO
import ssm.chaincode.dsl.model.SsmName

/**
 * Retrieves a list of the existing SSMs
 * @d2 function
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Query]
 * @title List SSMs
 * @order 20
 */
typealias SsmListSsmQueryFunction = F2Function<SsmListSsmQuery, SsmListSsmResult>

/**
 * @d2 query
 * @parent [SsmListSsmQueryFunction]
 * @title List SSMs: Parameters
 */
@Serializable
@JsExport
@JsName("SsmListSsmQuery")
class SsmListSsmQuery(
	override val chaincodeUri: ChaincodeUri
) : SsmQueryDTO

/**
 * @d2 event
 * @parent [SsmListSsmQueryFunction]
 * @title List SSMs: Result
 */
@Serializable
@JsExport
@JsName("SsmListSsmResult")
class SsmListSsmResult(
	/**
	 * Names of all known SSMs
	 * @example [["ProductLogistic", "Delivery", "Mobility"]]
	 */
	override val items: Array<SsmName>,
) : SsmItemsResultDTO<SsmName>
