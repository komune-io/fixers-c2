package ssm.couchdb.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cloud.function.context.FunctionCatalog
import ssm.bdd.spring.autoconfigure.ApplicationContextBuilder
import ssm.bdd.spring.autoconfigure.ApplicationContextRunnerBuilder
import ssm.couchdb.dsl.config.SsmCouchdbConfig


class CouchdbSsmApplicationContextRunnerTest {

    @Test
    fun `spring context runner must must start`() {

        ApplicationContextRunnerBuilder()
            .buildContext(
                SsmChaincodeConfigTest.localDockerComposeParams
            ).run { context ->
                assertThat(context).hasSingleBean(FunctionCatalog::class.java)
                assertThat(context).hasSingleBean(SsmCouchdbProperties::class.java)
                assertThat(context).hasBean(SsmCouchdbF2AutoConfiguration::couchdbDatabaseListQueryFunction.name)
                assertThat(context).hasBean(SsmCouchdbF2AutoConfiguration::couchdbDatabaseGetQueryFunction.name)
                assertThat(context).hasBean(SsmCouchdbF2AutoConfiguration::couchdbSsmListQueryFunction.name)
                assertThat(context).hasBean(SsmCouchdbF2AutoConfiguration::couchdbSsmSessionStateListQueryFunction.name)
            }
    }

    @Test
    fun `spring context must must start`() {
        val context = ApplicationContextBuilder().create(
            types = arrayOf(ApplicationContextBuilder.SimpleConfiguration::class.java),
            config = SsmChaincodeConfigTest.localDockerComposeParams
        )
        assertThat(context.getBean(SsmCouchdbF2AutoConfiguration::couchdbDatabaseListQueryFunction.name)).isNotNull
        val catalog = context.getBean(FunctionCatalog::class.java)
        assertThat(catalog).isNotNull
    }


    object SsmChaincodeConfigTest {
        val localDockerCompose = SsmCouchdbConfig(
            url = "http://localhost:5984",
            username = "couchdb",
            password = "couchdb",
            serviceName = "ssm-couchdb-test"
        )

        val localDockerComposeParams = mapOf(
            "ssm.couchdb.url" to localDockerCompose.url,
            "ssm.couchdb.username" to localDockerCompose.username,
            "ssm.couchdb.password" to localDockerCompose.password,
            "ssm.couchdb.serviceName" to localDockerCompose.serviceName
        )
    }
}
