package ssm.sdk.core.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmContext

data class SsmPerformCommand(
    val msgId: String,
    val action: String,
    val context: SsmContext,
    val chaincodeUri: ChaincodeUri,
    val signerName: AgentName,
)
