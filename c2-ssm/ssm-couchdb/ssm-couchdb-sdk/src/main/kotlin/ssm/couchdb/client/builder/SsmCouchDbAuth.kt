package ssm.couchdb.client.builder

sealed interface SsmCouchDbAuth

data class SsmCouchDbBasicAuth(
    val username: String,
    val password: String,
) : SsmCouchDbAuth
