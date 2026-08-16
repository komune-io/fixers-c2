package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.Ssm
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

class CreateCmd(ssm: Ssm) : CmdBuilder<Ssm>(ssm, SsmCmdName.CREATE)
