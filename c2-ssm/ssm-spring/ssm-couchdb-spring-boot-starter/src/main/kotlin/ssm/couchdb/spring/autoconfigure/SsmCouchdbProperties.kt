package ssm.couchdb.spring.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.couchdb.dsl.config.SsmCouchdbConfig

@ConfigurationProperties(prefix = "ssm")
data class SsmCouchdbProperties(
    val couchdb: SsmCouchdbConfig,
    val batch: SsmBatchProperties = SsmBatchProperties()
)
