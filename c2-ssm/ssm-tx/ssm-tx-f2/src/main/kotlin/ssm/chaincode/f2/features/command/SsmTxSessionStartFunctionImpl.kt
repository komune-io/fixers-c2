package ssm.chaincode.f2.features.command

import io.komune.c2.chaincode.dsl.burst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.SsmStartCommand
import ssm.tx.dsl.features.ssm.SsmSessionStartCommand
import ssm.tx.dsl.features.ssm.SsmSessionStartResult
import ssm.tx.dsl.features.ssm.SsmTxSessionStartFunction

class SsmTxSessionStartFunctionImpl(
	private val ssmTxService: SsmTxService
): SsmTxSessionStartFunction {

	override suspend fun invoke(msgs: Flow<SsmSessionStartCommand>): Flow<SsmSessionStartResult> = msgs.map { payload ->
		SsmStartCommand(
			session = payload.session,
			chaincodeUri = payload.chaincodeUri.burst(),
			signerName = payload.signerName
		)
	}.let {
		ssmTxService.sendStart(it).map { result ->
			SsmSessionStartResult(
				transactionId = result.transactionId,
			)
		}
	}
}
