package ssm.chaincode.f2.features.command

import io.komune.c2.chaincode.dsl.burst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.sdk.core.SsmTxService
import ssm.tx.dsl.features.ssm.SsmCreateCommand
import ssm.tx.dsl.features.ssm.SsmCreateResult
import ssm.tx.dsl.features.ssm.SsmTxCreateFunction

class SsmTxCreateFunctionImpl(
	private val ssmTxService: SsmTxService,
): SsmTxCreateFunction {

	override suspend fun invoke(msgs: Flow<SsmCreateCommand>): Flow<SsmCreateResult> = msgs.map { payload ->
		ssm.sdk.core.command.SsmCreateCommand(
			ssm = payload.ssm,
			chaincodeUri = payload.chaincodeUri.burst(),
			signerName = payload.signerName
		)
	}.let {
		ssmTxService.sendCreate(it).map { result ->
			SsmCreateResult(
				transactionId = result.transactionId,
			)
		}
	}
}
