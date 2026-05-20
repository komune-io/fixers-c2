package ssm.sdk.core.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmContext

data class SsmPerformCommand(
    override val chaincodeUri: ChaincodeUri,
    override val signerName: AgentName,
    val action: String,
    val context: SsmContext,
): WithSign
