package ssm.chaincode.spring.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.SsmChaincodeProperties

@ConfigurationProperties(prefix = "ssm")
data class SsmChaincodeConfiguration(
    val chaincode: SsmChaincodeProperties,
    val batch: SsmBatchProperties = SsmBatchProperties()
)
