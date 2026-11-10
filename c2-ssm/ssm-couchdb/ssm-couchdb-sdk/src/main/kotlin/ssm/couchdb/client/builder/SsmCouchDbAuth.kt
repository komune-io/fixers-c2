package ssm.couchdb.client.builder

sealed class SsmCouchDbAuth

data class SsmCouchDbBasicAuth(
    val username: String,
    val password: String,
) : SsmCouchDbAuth()
