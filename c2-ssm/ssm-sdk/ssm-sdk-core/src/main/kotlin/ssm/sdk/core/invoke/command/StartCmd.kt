package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.SsmSession
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

class StartCmd(session: SsmSession) : CmdBuilder<SsmSession>(session,  SsmCmdName.START)
