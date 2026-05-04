package ssm.chaincode.dsl.query

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUri
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.SsmQueryDTO
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog

/**
 * Retrieves the logs of a session since its creation
 * @d2 function
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2Query]
 * @title Get Session Logs
 * @order 20
 */
typealias SsmGetSessionLogsQueryFunction = F2Function<SsmGetSessionLogsQuery, SsmGetSessionLogsQueryResult>

/**
 * @d2 query
 * @parent [SsmGetSessionLogsQueryFunction]
 * @title Get Session Logs: Parameters
 */
@Serializable
@JsExport
@JsName("SsmGetSessionLogsQuery")
class SsmGetSessionLogsQuery(
    override val chaincodeUri: ChaincodeUri,
    /**
	 * The name of the ssm
	 * @example "ssm"
	 */
	val ssmName: SsmName,
    /**
	 * Identifier of the session to retrieve
	 * @example [SsmSessionState.session]
	 */
	val sessionName: SessionName,
) : SsmQueryDTO

/**
 * @d2 event
 * @parent [SsmGetSessionLogsQueryFunction]
 * @title Get Session Logs: Result
 */
@Serializable
@JsExport
@JsName("SsmGetSessionLogsQueryResult")
data class SsmGetSessionLogsQueryResult(
	val ssmName: SsmName,
	val sessionName: SessionName,
	val logs: List<SsmSessionStateLog>,
)
