package ssm.chaincode.f2.features.command

import io.komune.c2.chaincode.dsl.burst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.SsmPerformCommand
import ssm.tx.dsl.features.ssm.SsmSessionPerformActionCommand
import ssm.tx.dsl.features.ssm.SsmSessionPerformActionResult
import ssm.tx.dsl.features.ssm.SsmTxSessionPerformActionFunction

class SsmTxSessionPerformActionFunctionImpl(
	private val ssmTxService: SsmTxService
) : SsmTxSessionPerformActionFunction {

	override suspend fun invoke(
		msgs: Flow<SsmSessionPerformActionCommand>
	): Flow<SsmSessionPerformActionResult> = msgs.map { payload ->
		SsmPerformCommand(
			action = payload.action,
			context = payload.context,
			chaincodeUri = payload.chaincodeUri.burst(),
			signerName = payload.signerName
		)
	}.let {
		ssmTxService.sendPerform(it).map { result ->
			SsmSessionPerformActionResult(
				transactionId = result.transactionId,
			)
		}
	}
}
