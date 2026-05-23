package ssm.tx.dsl

import ssm.tx.dsl.features.ssm.SsmTxCreateFunction
import ssm.tx.dsl.features.ssm.SsmTxInitFunction
import ssm.tx.dsl.features.user.SsmTxUserGrantFunction
import ssm.tx.dsl.features.user.SsmTxUserRegisterFunction

/**
 * - fun ssmTxUserGrantFunction(): [SsmTxUserGrantFunction]
 * - fun ssmTxUserRegisterFunction(): [SsmTxUserRegisterFunction]
 *
 * - fun ssmTxCreateFunction(): [SsmTxCreateFunction]
 * - fun ssmTxInitializeFunction(): [SsmTxInitFunction]
 *
 * Session start/perform F2 functions are JVM-only (see
 * `ssm.chaincode.f2.features.command.SsmTxSessionStartFunction` and
 * `SsmTxSessionPerformActionFunction` in `ssm-tx-f2`).
 *
 * @d2 model
 * @title Admin Agent command
 * @parent [ssm.tx.dsl.SsmTxD2]
 */
interface SsmTxAdminFunctions {
	fun ssmTxUserGrantFunction(): SsmTxUserGrantFunction
	fun ssmTxUserRegisterFunction(): SsmTxUserRegisterFunction

	fun ssmTxCreateFunction(): SsmTxCreateFunction
	fun ssmTxInitializeFunction(): SsmTxInitFunction
}
