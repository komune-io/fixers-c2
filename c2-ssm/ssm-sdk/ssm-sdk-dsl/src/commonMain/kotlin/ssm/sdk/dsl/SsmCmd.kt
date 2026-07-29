package ssm.sdk.dsl

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName

data class SsmCmd(
    val chaincodeUri: ChaincodeUri,
    val agentName: AgentName,
    val json: String,
    val command: SsmCmdName,
    val performAction: String? = null,
    val valueToSign: String,
    /**
     * Optional business timestamp (epoch millis). Carried as transport metadata down to the Fabric
     * transaction envelope; deliberately excluded from [valueToSign] so the SSM signature is unaffected.
     */
    val timestamp: Long? = null,
)
