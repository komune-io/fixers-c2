package ssm.chaincode.f2.features.command

import io.komune.c2.chaincode.dsl.burst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.UserRegisterCommand
import ssm.tx.dsl.features.user.SsmTxUserRegisterFunction
import ssm.tx.dsl.features.user.SsmUserRegisterCommand
import ssm.tx.dsl.features.user.SsmUserRegisteredResult

class SsmUserRegisterFunctionImpl(
	private val ssmTxService: SsmTxService
): SsmTxUserRegisterFunction {

	override suspend fun invoke(msgs: Flow<SsmUserRegisterCommand>): Flow<SsmUserRegisteredResult> = msgs.map { payload ->
		UserRegisterCommand(
			agent = payload.agent,
			chaincodeUri = payload.chaincodeUri.burst(),
			signerName = payload.signerName
		)
	}.let {
		ssmTxService.sendRegisterUser(it).map { result ->
			SsmUserRegisteredResult(
				transactionId = result.transactionId,
			)
		}
	}

}
