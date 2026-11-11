package ssm.couchdb.spring.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.couchdb.dsl.SsmCouchDbQueries
import ssm.couchdb.dsl.config.SsmCouchdbConfig
import ssm.couchdb.dsl.query.CouchdbAdminListQueryFunction
import ssm.couchdb.dsl.query.CouchdbChaincodeListQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbDatabaseListQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmListQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryFunction
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryFunction
import ssm.couchdb.dsl.query.CouchdbUserListQueryFunction
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl

@ConditionalOnProperty(prefix = "ssm.couchdb", name = ["url"])
@EnableConfigurationProperties(SsmCouchdbProperties::class)
@Configuration(proxyBeanMethods = false)
class SsmCouchdbAutoConfiguration {

    @Bean
    fun couchdbConfig(
        ssmCouchdbProperties: SsmCouchdbProperties,
    ): SsmCouchdbConfig = ssmCouchdbProperties.couchdb

    @Bean
    fun ssmCouchDbQueries(
        ssmCouchdbConfig: SsmCouchdbConfig,
    ): SsmCouchDbQueries = CouchdbSsmQueriesFunctionImpl(ssmCouchdbConfig)

}

// Bean names are the F2 function names consumers resolve: shortened methods keep them explicitly.
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(SsmCouchDbQueries::class)
class SsmCouchdbF2AutoConfiguration(
    private val ssmCouchDbQueries: SsmCouchDbQueries,
) {

    @Bean("couchdbDatabaseGetChangesQueryFunction")
    fun databaseChangesQueryFunction(): CouchdbDatabaseGetChangesQueryFunction =
        ssmCouchDbQueries.couchdbDatabaseGetChangesQueryFunction()

    @Bean("couchdbDatabaseListQueryFunction")
    fun databaseListQueryFunction(): CouchdbDatabaseListQueryFunction =
        ssmCouchDbQueries.couchdbDatabaseListQueryFunction()

    @Bean("couchdbDatabaseGetQueryFunction")
    fun databaseGetQueryFunction(): CouchdbDatabaseGetQueryFunction =
        ssmCouchDbQueries.couchdbDatabaseGetQueryFunction()

    @Bean("couchdbChaincodeListQueryFunction")
    fun chaincodeListQueryFunction(): CouchdbChaincodeListQueryFunction =
        ssmCouchDbQueries.couchdbChaincodeListQueryFunction()

    @Bean
    fun couchdbAdminListQueryFunction(): CouchdbAdminListQueryFunction =
        ssmCouchDbQueries.couchdbAdminListQueryFunction()

    @Bean
    fun couchdbUserListQueryFunction(): CouchdbUserListQueryFunction = ssmCouchDbQueries.couchdbUserListQueryFunction()

    @Bean
    fun couchdbSsmGetQueryFunction(): CouchdbSsmGetQueryFunction = ssmCouchDbQueries.couchdbSsmGetQueryFunction()

    @Bean
    fun couchdbSsmListQueryFunction(): CouchdbSsmListQueryFunction = ssmCouchDbQueries.couchdbSsmListQueryFunction()

    @Bean("couchdbSsmSessionStateListQueryFunction")
    fun sessionStateListQueryFunction(): CouchdbSsmSessionStateListQueryFunction =
        ssmCouchDbQueries.couchdbSsmSessionStateListQueryFunction()

    @Bean("couchdbSsmSessionStateGetQueryFunction")
    fun sessionStateGetQueryFunction(): CouchdbSsmSessionStateGetQueryFunction =
        ssmCouchDbQueries.couchdbSsmSessionStateGetQueryFunction()
}
