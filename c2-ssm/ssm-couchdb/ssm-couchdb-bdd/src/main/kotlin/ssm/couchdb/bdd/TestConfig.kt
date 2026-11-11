package ssm.couchdb.bdd

import ssm.couchdb.dsl.config.SsmCouchdbConfig

object TestConfig {
    val dbConfig = SsmCouchdbConfig(
        url = "http://localhost:5984",
        username = "couchdb",
        password = "couchdb",
        serviceName = "ssm-couchdb-unit"
    )
    const val CHANNEL_ID = "sandbox"
    const val CHAINCODE_ID = "ssm"
}
