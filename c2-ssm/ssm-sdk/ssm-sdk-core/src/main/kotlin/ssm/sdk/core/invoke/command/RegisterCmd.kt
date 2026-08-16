package ssm.sdk.core.invoke.command

import ssm.chaincode.dsl.model.Agent
import ssm.sdk.core.invoke.builder.CmdBuilder
import ssm.sdk.dsl.SsmCmdName

class RegisterCmd(agent: Agent) : CmdBuilder<Agent>(agent, SsmCmdName.REGISTER)
