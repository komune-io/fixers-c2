package ssm.sdk.core.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmSession

data class SsmStartCommand(
    val msgId: String,
    val session: SsmSession,
    val chaincodeUri: ChaincodeUri,
    val signerName: AgentName,
)
