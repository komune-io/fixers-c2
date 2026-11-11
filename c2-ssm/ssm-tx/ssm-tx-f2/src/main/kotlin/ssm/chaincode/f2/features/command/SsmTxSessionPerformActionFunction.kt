package ssm.chaincode.f2.features.command

import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.Flow
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.dsl.CommandOutcome

typealias SsmTxSessionPerformActionFunction = F2Function<SsmPerformCommand, CommandOutcome>

class SsmTxSessionPerformActionFunctionImpl(
    private val ssmTxService: SsmTxService,
) : SsmTxSessionPerformActionFunction {
    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun invoke(msgs: Flow<SsmPerformCommand>): Flow<CommandOutcome> =
        ssmTxService.sendPerform(msgs)
}
