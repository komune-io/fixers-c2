package ssm.tx.config.spring.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.sdk.sign.model.SignerAdmin

@ConfigurationProperties(prefix = "ssm")
data class SsmTxProperties(
    val chaincode: SsmChaincodeProperties?,
    val signer: SignerFileConfig?,
    val batch: SsmBatchProperties = SsmBatchProperties()
) {
    data class SignerFileConfig(
        val admin: SignerAgentFileConfig?,
        val user: SignerAgentFileConfig?,
    )
    data class SignerAgentFileConfig(
        val name: String,
        val key: String,
    )
}

fun SsmTxProperties.SignerAgentFileConfig.signer(): SignerAdmin = SignerAdmin.loadFromFile(name, key)
