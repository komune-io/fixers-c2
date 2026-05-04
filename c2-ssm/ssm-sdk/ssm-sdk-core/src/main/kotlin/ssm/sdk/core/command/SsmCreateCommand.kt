package ssm.sdk.core.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm

data class SsmCreateCommand(
    override val chaincodeUri: ChaincodeUri,
    override val signerName: AgentName,
    val ssm: Ssm,
): WithSign
