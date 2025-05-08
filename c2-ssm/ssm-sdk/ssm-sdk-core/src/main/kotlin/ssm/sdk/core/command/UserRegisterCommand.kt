package ssm.sdk.core.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName

data class UserRegisterCommand(
    override val chaincodeUri: ChaincodeUri,
    override val signerName: AgentName,
    val agent: Agent,
): WithSign
