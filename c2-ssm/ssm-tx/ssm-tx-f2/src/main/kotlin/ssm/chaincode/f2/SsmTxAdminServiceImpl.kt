package ssm.chaincode.f2

import ssm.chaincode.f2.features.command.SsmTxCreateFunctionImpl
import ssm.chaincode.f2.features.command.SsmTxInitFunctionImpl
import ssm.chaincode.f2.features.command.SsmUserGrantFunctionImpl
import ssm.chaincode.f2.features.command.SsmUserRegisterFunctionImpl
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.tx.dsl.SsmTxAdminFunctions
import ssm.tx.dsl.features.ssm.SsmTxCreateFunction
import ssm.tx.dsl.features.ssm.SsmTxInitFunction
import ssm.tx.dsl.features.user.SsmTxUserGrantFunction
import ssm.tx.dsl.features.user.SsmTxUserRegisterFunction

class SsmTxAdminServiceImpl(
	private val ssmTxService: SsmTxService,
	private val ssmQueryService: SsmQueryService,
) : SsmTxAdminFunctions {

	override fun ssmTxUserGrantFunction(): SsmTxUserGrantFunction {
		return SsmUserGrantFunctionImpl(ssmTxService)
	}

	override fun ssmTxUserRegisterFunction(): SsmTxUserRegisterFunction {
		return SsmUserRegisterFunctionImpl(ssmTxService)
	}

	override fun ssmTxCreateFunction(): SsmTxCreateFunction {
		return SsmTxCreateFunctionImpl(ssmTxService)
	}

	override fun ssmTxInitializeFunction(): SsmTxInitFunction {
		return SsmTxInitFunctionImpl(ssmTxService, ssmQueryService)
	}
}
