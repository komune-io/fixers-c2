package ssm.chaincode.dsl

import kotlin.js.JsExport
import kotlin.js.JsName
import ssm.chaincode.dsl.query.SsmGetAdminFunction
import ssm.chaincode.dsl.query.SsmGetQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionQueryFunction
import ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction
import ssm.chaincode.dsl.query.SsmGetUserFunction
import ssm.chaincode.dsl.query.SsmListAdminQueryFunction
import ssm.chaincode.dsl.query.SsmListSessionQueryFunction
import ssm.chaincode.dsl.query.SsmListSsmQueryFunction
import ssm.chaincode.dsl.query.SsmListUserQueryFunction

/**
 * Query functions of the SSM chaincode.
 *
 * - fun ssmGetAdminFunction(): [ssm.chaincode.dsl.query.SsmGetAdminFunction]
 * - fun ssmGetQueryFunction(): [ssm.chaincode.dsl.query.SsmGetQueryFunction]
 * - fun ssmGetSessionLogsQueryFunction(): [ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction]
 * - fun ssmGetSessionQueryFunction(): [ssm.chaincode.dsl.query.SsmGetSessionQueryFunction]
 * - fun ssmGetTransactionQueryFunction(): [ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction]
 * - fun ssmGetUserFunction(): [ssm.chaincode.dsl.query.SsmGetUserFunction]
 * - fun ssmListAdminQueryFunction(): [ssm.chaincode.dsl.query.SsmListAdminQueryFunction]
 * - fun ssmListSessionQueryFunction(): [ssm.chaincode.dsl.query.SsmListSessionQueryFunction]
 * - fun ssmListSsmQueryFunction(): [ssm.chaincode.dsl.query.SsmListSsmQueryFunction]
 * - fun ssmListUserQueryFunction(): [ssm.chaincode.dsl.query.SsmListUserQueryFunction]
 * @d2 model
 * @title Query function
 * @parent [ssm.chaincode.dsl.SsmChaincodeD2]
 */
@JsExport
@JsName("SsmChaincodeQueries")
interface SsmChaincodeQueries {
    fun ssmGetAdminFunction(): SsmGetAdminFunction
    fun ssmGetQueryFunction(): SsmGetQueryFunction
    fun ssmGetSessionLogsQueryFunction(): SsmGetSessionLogsQueryFunction
    fun ssmGetSessionQueryFunction(): SsmGetSessionQueryFunction
    fun ssmGetTransactionQueryFunction(): SsmGetTransactionQueryFunction
    fun ssmGetUserFunction(): SsmGetUserFunction
    fun ssmListAdminQueryFunction(): SsmListAdminQueryFunction
    fun ssmListSessionQueryFunction(): SsmListSessionQueryFunction
    fun ssmListSsmQueryFunction(): SsmListSsmQueryFunction
    fun ssmListUserQueryFunction(): SsmListUserQueryFunction
}
