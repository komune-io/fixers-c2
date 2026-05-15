package ssm.sdk.core.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession

data class SsmStartCommandV2(
    val commandId: String,
    val session: SsmSession,
    val chaincodeUri: ChaincodeUri,
    val signerName: AgentName,
)

data class SsmPerformCommandV2(
    val commandId: String,
    val action: String,
    val context: SsmContext,
    val chaincodeUri: ChaincodeUri,
    val signerName: AgentName,
)
