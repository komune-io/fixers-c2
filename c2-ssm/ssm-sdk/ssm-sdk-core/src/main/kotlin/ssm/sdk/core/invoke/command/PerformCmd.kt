package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.SsmContextDTO
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

class PerformCmd(performAction: String, context: SsmContextDTO) :
	CmdBuilder<SsmContextDTO?>(context, SsmCmdName.PERFORM, performAction)
