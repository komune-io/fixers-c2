package ssm.data.spring.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.couchdb.dsl.config.SsmCouchdbConfig

@ConfigurationProperties(prefix = "ssm")
data class SsmDataProperties(
    /**
     * Configuration for couchdb.
     */
    val couchdb: SsmCouchdbConfig?,
    /**
     *  Configuration for couchdb.
     */
    val chaincode: SsmChaincodeProperties?,
    /**
     * Configuration for chunking.
     */
    val batch: SsmBatchProperties = SsmBatchProperties(),
)
