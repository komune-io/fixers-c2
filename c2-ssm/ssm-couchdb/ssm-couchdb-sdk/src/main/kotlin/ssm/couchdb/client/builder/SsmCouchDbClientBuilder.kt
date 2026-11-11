package ssm.couchdb.client.builder

import com.ibm.cloud.sdk.core.security.BasicAuthenticator
import java.util.logging.Logger
import ssm.couchdb.client.CouchdbSsmClient
import ssm.sdk.json.JSONConverter
import ssm.sdk.json.JSONConverterObjectMapper

class SsmCouchDbClientBuilder {

    private val logger = Logger.getLogger(SsmCouchDbClientBuilder::class.java.name)
    private var jsonConverter: JSONConverter = JSONConverterObjectMapper()
    private var auth: SsmCouchDbAuth? = null
    private var url: String? = null
    private var name: String? = null

    fun withJSONConverter(jsonConverter: JSONConverter): SsmCouchDbClientBuilder = apply {
        this.jsonConverter = jsonConverter
    }

    fun withUrl(url: String): SsmCouchDbClientBuilder = apply {
        this.url = url
    }

    fun withName(name: String): SsmCouchDbClientBuilder = apply {
        this.name = name
    }

    fun withAuth(auth: SsmCouchDbAuth): SsmCouchDbClientBuilder = apply {
        this.auth = auth
    }

    fun build(): CouchdbSsmClient {
        val auth = checkNotNull(this.auth) { "auth is not set" }
        val cloudant = buildCloudant(auth)
        return CouchdbSsmClient(cloudant, jsonConverter)
    }

    private fun buildCloudant(auth: SsmCouchDbAuth): CloudantFixed {
        val cloudantAuth = when (auth) {
            is SsmCouchDbBasicAuth -> BasicAuthenticator.Builder()
                .username(auth.username)
                .password(auth.password)
                .build()
        }

        val client = CloudantFixed(checkNotNull(name) { "name is not set" }, cloudantAuth).apply {
            serviceUrl = checkNotNull(url) { "url is not set" }
        }

        val serverInformation = client
            .serverInformation
            .execute()
            .result
        logger.info { "Server Version: ${serverInformation.version}" }
        return client
    }
}
