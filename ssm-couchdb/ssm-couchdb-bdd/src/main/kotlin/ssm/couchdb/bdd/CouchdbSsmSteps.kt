package ssm.couchdb.bdd

import f2.dsl.fnc.invokeWith
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import ssm.bdd.config.SsmQueryStep
import ssm.bdd.config.contextualize
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.SsmSessionStateDTO
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.burstChaincode
import ssm.couchdb.dsl.model.DocTypeName
import ssm.couchdb.dsl.query.CouchdbAdminListQuery
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQuery
import ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryResultDTO
import ssm.couchdb.dsl.query.CouchdbSsmListQuery
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQuery
import ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQuery
import ssm.couchdb.dsl.query.CouchdbUserListQuery
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl

class CouchdbSsmSteps : SsmQueryStep(), En {
	companion object {
		const val GLUE = "ssm.couchdb.bdd"
	}

	var lastChanges: CouchdbDatabaseGetChangesQueryResultDTO? = null

	val couchdbSsmQueriesFunctions = CouchdbSsmQueriesFunctionImpl(
		TestConfig.dbConfig
	)

	init {
		prepareSteps()

		When("I get changed for session {string} for {string}") { sessionName: SessionName, ssmName: SsmName ->
			runBlocking {
				lastChanges = getChanges(bag.chaincodeUri, ssmName.contextualize(bag), sessionName.contextualize(bag))
				println(lastChanges)
			}
		}
		Then("Changes for session {string} for {string} is") { sessionName: SessionName, ssmName: SsmName, dataTable: DataTable ->
			runBlocking {

				delay(timeMillis = 1500)
				lastChanges = getChanges(bag.chaincodeUri, ssmName.contextualize(bag), sessionName.contextualize(bag))
				Assertions.assertThat(dataTable.asCucumberChanges().size).isEqualTo(lastChanges?.items?.size ?: 0.0)
				val tt = lastChanges!!.items.associateBy { it.objectId }
				dataTable.asCucumberChanges().forEachIndexed { index, clog ->
					val log = tt[clog.objectId.contextualize(bag)]!!
					Assertions.assertThat(log.objectId).isEqualTo(clog.objectId.contextualize(bag))
					Assertions.assertThat(log.docType!!.name).isEqualTo(clog.docType)
				}
			}
		}
		Then("Changes for session {string} for {string} is empty") { sessionName: SessionName, ssmName: SsmName ->
			runBlocking {
				lastChanges = getChanges(bag.chaincodeUri, ssmName.contextualize(bag), sessionName.contextualize(bag))
				Assertions.assertThat(lastChanges!!.items).isEmpty()

			}
		}
	}

	fun DataTable.asCucumberChanges(): List<CucumberChanges> {
		return asMaps().map { columns ->
			CucumberChanges(
				objectId = columns[CucumberChanges::objectId.name]!!,
				docType = columns[CucumberChanges::docType.name]!!,
			)
		}
	}

	suspend fun getChanges(
		chaincodeUri: ChaincodeUri,
		ssmName: SsmName,
		sessionName: SessionName
	): CouchdbDatabaseGetChangesQueryResultDTO {
		val uri = chaincodeUri.burstChaincode()!!
		return CouchdbDatabaseGetChangesQuery(
			channelId = uri.channelId,
			chaincodeId = uri.chaincodeId,
			ssmName = ssmName,
			sessionName = sessionName,
			docType = null,
			lastEventId = lastChanges?.lastEventId
		).invokeWith(couchdbSsmQueriesFunctions.couchdbDatabaseGetChangesQueryFunction())
	}

	override suspend fun getSession(ssmName: SsmName, sessionName: SessionName): SsmSessionStateDTO {
		return CouchdbSsmSessionStateGetQuery(
			chaincodeUri = bag.chaincodeUri,
			ssmName = ssmName,
			sessionName = sessionName
		).invokeWith(couchdbSsmQueriesFunctions.couchdbSsmSessionStateGetQueryFunction())
			.item
	}

	override suspend fun logSession(sessionName: String): List<SsmSessionStateLog> {
		return bag.clientQuery.log(sessionName)
	}

	override suspend fun listSessions(): List<SessionName> {
		return CouchdbSsmSessionStateListQuery(chaincodeUri = bag.chaincodeUri)
			.invokeWith(couchdbSsmQueriesFunctions.couchdbSsmSessionStateListQueryFunction())
			.page.list
			.map { it.session }
	}

	override suspend fun listSsm(): List<SsmName> {
		val uri = bag.chaincodeUri.burstChaincode()!!
		return CouchdbSsmListQuery(
			pagination = null,
			channelId = uri.channelId,
			chaincodeId = uri.chaincodeId
		).invokeWith(couchdbSsmQueriesFunctions.couchdbSsmListQueryFunction())
			.page.list
			.map { it.name }
	}

	override suspend fun listUsers(): List<String> {
		return CouchdbUserListQuery(bag.chaincodeUri)
			.invokeWith(couchdbSsmQueriesFunctions.couchdbUserListQueryFunction())
			.items.map { it.name }
	}

	override suspend fun listAdmins(): List<String> {
		return CouchdbAdminListQuery(bag.chaincodeUri)
			.invokeWith(couchdbSsmQueriesFunctions.couchdbAdminListQueryFunction())
			.items.map { it.name }
	}
}


class CucumberChanges(
	val objectId: String,
	val docType: DocTypeName,
)
