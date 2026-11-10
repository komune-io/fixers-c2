package ssm.couchdb.bdd

import f2.dsl.fnc.invoke
import io.cucumber.java8.En
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import ssm.chaincode.dsl.model.Chaincode
import ssm.chaincode.dsl.model.Ssm
import ssm.couchdb.dsl.config.SsmCouchdbConfig
import ssm.couchdb.dsl.model.DatabaseDTO
import ssm.couchdb.dsl.query.CouchdbDatabaseGetQuery
import ssm.couchdb.dsl.query.CouchdbDatabaseListQuery
import ssm.couchdb.dsl.query.CouchdbSsmListQuery
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl

class DatabasesSteps : En {

    private lateinit var config: SsmCouchdbConfig
    private var databases: List<DatabaseDTO> = emptyList()
    private var ssms: List<Ssm> = emptyList()

    init {
        DataTableType { entry: Map<String, String> ->
            Chaincode(
                id = entry["chaincodeId"] ?: "",
                channelId = entry["channelId"] ?: ""
            )
        }

        Given("I have a local database") {
            config = TestConfig.dbConfig
        }

        When("I get the list of databases") {
            runBlocking {
                databases = CouchdbSsmQueriesFunctionImpl(config).couchdbDatabaseListQueryFunction().invoke(
                    CouchdbDatabaseListQuery()
                ).items
            }
        }

        When("I get the list of databases filtered by {string} with {string}") { filter: String, value: String ->
            runBlocking {
                val query = when (filter) {
                    CouchdbDatabaseListQuery::channelId.name -> CouchdbDatabaseListQuery(
                        channelId = value,
                        chaincodeId = null
                    )
                    CouchdbDatabaseListQuery::chaincodeId.name -> CouchdbDatabaseListQuery(
                        channelId = null,
                        chaincodeId = value
                    )
                    else -> CouchdbDatabaseListQuery(null, null)
                }
                databases = CouchdbSsmQueriesFunctionImpl(config).couchdbDatabaseListQueryFunction().invoke(
                    query
                ).items
            }
        }

        When("I get the database for") { chaincode: Chaincode ->
            runBlocking {
                databases = CouchdbSsmQueriesFunctionImpl(config).couchdbDatabaseGetQueryFunction().invoke(
                    CouchdbDatabaseGetQuery(
                        channelId = chaincode.channelId,
                        chaincodeId = chaincode.id,
                    )
                ).item.let {
                    listOfNotNull(it)
                }
            }
        }

        Then("I found the database {string}") { name: String ->
            Assertions.assertThat(databases.map { it.name }).contains(name)
        }

        Then("I do not found database") {
            Assertions.assertThat(databases).isEmpty()
        }

        When("I get all ssm for") { chaincode: Chaincode ->
            runBlocking {
                ssms = CouchdbSsmQueriesFunctionImpl(config)
                    .couchdbSsmListQueryFunction()
                    .invoke(CouchdbSsmListQuery(
                        channelId = chaincode.channelId,
                        chaincodeId = chaincode.id,
                        pagination = null
                    )).items
            }
        }
        Then("I do not found ssm") {
            Assertions.assertThat(ssms).isEmpty()
        }
        Then("I found ssm") {
            Assertions.assertThat(ssms).isNotEmpty
        }
    }
}
