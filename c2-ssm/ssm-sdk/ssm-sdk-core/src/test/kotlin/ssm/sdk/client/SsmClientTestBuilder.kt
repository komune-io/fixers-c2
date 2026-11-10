package ssm.sdk.client

import java.io.IOException
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.sdk.core.SsmServiceFactory

object SsmClientTestBuilder {

//	private val uuid = UUID.randomUUID().toString()
//	private const val NETWORK = "bclan-it/"

    @Throws(IOException::class)
    fun build(): SsmServiceFactory {
        return SsmServiceFactory.builder(configFileName, SsmBatchProperties())
    }

    private val configFileName: String
        get() {
            val profile = System.getenv("PROFILES_ACTIVE")
            println("////////////////////////////")
            println(profile)
            println("////////////////////////////")
            return if ("gitlab".equals(profile, ignoreCase = true)) {
                "ssm-client-gitlab.properties"
            } else "ssm-client.properties"
        }
}
