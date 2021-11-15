package ssm.bdd.config

import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import io.cucumber.java8.Scenario
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import ssm.chaincode.dsl.config.SsmChaincodeConfig
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmAction
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.SsmRole
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateDTO
import ssm.chaincode.dsl.model.SsmSessionStateLog

abstract class SsmQueryStep {

	lateinit var bag: SsmCucumberBag
	lateinit var config: SsmChaincodeConfig

	@Suppress("LongMethod")
	fun En.prepareSteps() {
		Before { scenario: Scenario ->
			bag = SsmCucumberBag.init(scenario)
			config = SsmChaincodeConfig(
				bag.config.baseUrl
			)
		}

		When("I get the list of admin") {
			bag.adminsName = runBlocking {
				listAdmins()
			}
		}

		When("I get the list of user") {
			runBlocking {
				bag.usersName = listUsers()
			}
		}

		When("I get the list of ssm") {
			runBlocking {
				bag.ssmsName = listSsm()
			}
		}
		When("I get the list of session") {
			runBlocking {
				bag.sessions = listSessions()
			}
		}
		Then("Admin {string} is in the list") { adminName: String ->
			runBlocking {
				Assertions.assertThat(
					listAdmins()
				).contains(adminName)
			}
		}
		Then("User {string} is in the list") { userName: String ->
			runBlocking {
				Assertions.assertThat(
					listUsers()
				).contains(userName.contextualize(bag))
			}
		}
		Then("Ssm {string} is in the list") { ssmName: SsmName ->
			runBlocking {
				Assertions.assertThat(
					listSsm()
				).contains(ssmName.contextualize(bag))
			}
		}
		Then("Session {string} is in the list") { sessionName: SessionName ->
			runBlocking {
				Assertions.assertThat(
					listSessions()
				).contains(sessionName.contextualize(bag))
			}
		}
		Then("Session {string} for {string} have current state origin {string} current {string} iteration {string}")
		{ sessionName: SessionName, ssmName: SsmName,origin: String, current: String, iteration: String ->
			runBlocking {
				Assertions.assertThat(getSession(ssmName, sessionName)).hasFieldOrProperty(SsmSessionState::origin.name)
					.isEqualTo(origin)
					.hasFieldOrProperty(SsmSessionState::current.name).isEqualTo(current)
					.hasFieldOrProperty(SsmSessionState::iteration.name).isEqualTo(iteration)
			}
		}

		Then("Admin {string} is not in the list") { adminName: String ->
			runBlocking {
				Assertions.assertThat(
					listAdmins()
				).doesNotContain(adminName)
			}
		}
		Then("User {string} is not in the list") { userName: String ->
			runBlocking {
				Assertions.assertThat(
					listUsers()
				).doesNotContain(userName.contextualize(bag))
			}
		}
		Then("Ssm {string} is not in the list") { ssmName: SsmName ->
			runBlocking {
				Assertions.assertThat(
					listSsm()
				).doesNotContain(ssmName.contextualize(bag))
			}
		}

		Then("The action {string} has been performed for session {string}")
		{ actionName: String, sessionName: SessionName ->
			runBlocking {
				Assertions.assertThat(
					logSession(sessionName).mapNotNull { it.state.origin }.map { it.action }
				).contains(actionName)
			}
		}

		Then("The session {string} have log with {string} entries")
		{ sessionName: SessionName, nbEntries: String ->
			runBlocking {
				Assertions.assertThat(
					logSession(sessionName.contextualize(bag))
				).hasSize(nbEntries.toInt())
			}
		}

		Then("The session {string} for {string} have logs") { sessionName: SessionName, ssmName: SsmName, table: DataTable ->
			runBlocking {
				val logs = logSession(sessionName.contextualize(bag)).sortedBy { it.state.iteration }
				table.asCucumberSessionLog().forEachIndexed { index, clog ->
					val log = logs[index]
					Assertions.assertThat(log.state.origin?.action).isEqualTo(clog.originAction)
					Assertions.assertThat(log.state.origin?.from).isEqualTo(clog.originFrom)
					Assertions.assertThat(log.state.origin?.to).isEqualTo(clog.originTo)
					Assertions.assertThat(log.state.origin?.role).isEqualTo(clog.originRole)
					Assertions.assertThat(log.state.current).isEqualTo(clog.current)
					Assertions.assertThat(log.state.iteration).isEqualTo(clog.iteration)
					Assertions.assertThat(log.state.ssm).isEqualTo(ssmName.contextualize(bag))
					Assertions.assertThat(log.state.session).isEqualTo(sessionName.contextualize(bag))
				}
			}
		}
	}

	fun DataTable.asCucumberSessionLog(): List<CucumberSessionLog> {
		return asMaps().map { columns ->
			CucumberSessionLog(
				current = columns[CucumberSessionLog::current.name]?.toInt()!!,
				iteration = columns[CucumberSessionLog::iteration.name]?.toInt()!!,
				originAction = columns[CucumberSessionLog::originAction.name],
				originFrom = columns[CucumberSessionLog::originFrom.name]?.toInt(),
				originTo = columns[CucumberSessionLog::originTo.name]?.toInt(),
				originRole = columns[CucumberSessionLog::originRole.name],
			)
		}
	}

	protected abstract suspend fun getSession(ssmName: SsmName, sessionName: SessionName): SsmSessionStateDTO?

	protected abstract suspend fun logSession(sessionName: SessionName): List<SsmSessionStateLog>

	protected abstract suspend fun listSessions(): List<SessionName>

	protected abstract suspend fun listSsm(): List<SsmName>

	protected abstract suspend fun listUsers(): List<String>

	protected abstract suspend fun listAdmins(): List<String>
}

class CucumberSessionLog(
	val current: Int,
	val iteration: Int,
	val originAction: SsmAction?,
	val originFrom: Int?,
	val originTo: Int?,
	val originRole: SsmRole?,
)
