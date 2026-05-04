package ssm.sdk.core.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmSession

data class SsmStartCommand(
    override val chaincodeUri: ChaincodeUri,
    override val signerName: AgentName,
    val session: SsmSession,
): WithSign
