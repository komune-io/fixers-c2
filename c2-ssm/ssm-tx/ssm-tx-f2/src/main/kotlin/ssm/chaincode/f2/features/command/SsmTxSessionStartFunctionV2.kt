package ssm.chaincode.f2.features.command

import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.Flow
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.SsmStartCommandV2
import ssm.sdk.dsl.CommandOutcome

typealias SsmTxSessionStartFunctionV2 = F2Function<SsmStartCommandV2, CommandOutcome>

class SsmTxSessionStartFunctionV2Impl(
    private val ssmTxService: SsmTxService,
) : SsmTxSessionStartFunctionV2 {
    override suspend fun invoke(msgs: Flow<SsmStartCommandV2>): Flow<CommandOutcome> =
        ssmTxService.sendStartV2(msgs)
}
