package ssm.chaincode.f2.features.command

import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.Flow
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.SsmPerformCommandV2
import ssm.sdk.dsl.CommandOutcome

typealias SsmTxSessionPerformActionFunctionV2 = F2Function<SsmPerformCommandV2, CommandOutcome>

class SsmTxSessionPerformActionFunctionV2Impl(
    private val ssmTxService: SsmTxService,
) : SsmTxSessionPerformActionFunctionV2 {
    override suspend fun invoke(msgs: Flow<SsmPerformCommandV2>): Flow<CommandOutcome> =
        ssmTxService.sendPerformV2(msgs)
}
