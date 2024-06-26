package ssm.sdk.core.command

import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.uri.ChaincodeUri

data class SsmPerformCommand(
    override val chaincodeUri: ChaincodeUri,
    override val signerName: AgentName,
    val action: String,
    val context: SsmContext,
): WithSign
