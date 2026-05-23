package ssm.chaincode.f2.features.command

import f2.dsl.fnc.F2Function
import kotlinx.coroutines.flow.Flow
import ssm.sdk.core.SsmTxService
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.dsl.CommandOutcome

typealias SsmTxSessionStartFunction = F2Function<SsmStartCommand, CommandOutcome>

class SsmTxSessionStartFunctionImpl(
    private val ssmTxService: SsmTxService,
) : SsmTxSessionStartFunction {
    override suspend fun invoke(msgs: Flow<SsmStartCommand>): Flow<CommandOutcome> =
        ssmTxService.sendStart(msgs)
}
