package ssm.sdk.client

import java.io.IOException
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.sdk.core.SsmServiceFactory

object SsmClientTestBuilder {

    private val configFileName: String
        get() {
            val profile = System.getenv("PROFILES_ACTIVE")
            return if ("gitlab".equals(profile, ignoreCase = true)) {
                "ssm-client-gitlab.properties"
            } else {
                "ssm-client.properties"
            }
        }

    @Throws(IOException::class)
    fun build(): SsmServiceFactory = SsmServiceFactory.builder(configFileName, SsmBatchProperties())
}
