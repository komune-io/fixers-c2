package ssm.bdd.config

import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import io.cucumber.java8.Scenario
import kotlinx.coroutines.runBlocking
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmAction
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmTransition
import ssm.sdk.core.extention.SsmJsonReader
import ssm.sdk.sign.extention.asAgent
import ssm.sdk.sign.model.SignerAdmin
import ssm.sdk.sign.model.SignerUser

abstract class SsmCommandStep {

	companion object {
		private const val DEFAULT_ADMIN_NAME = "ssm-admin"
		private const val DEFAULT_ADMIN_KEY = "local/admin/ssm-admin"
	}

	lateinit var bag: SsmCucumberBag
	lateinit var config: SsmChaincodeProperties

	@Suppress("LongMethod")
	fun En.prepareSteps() {
		Before { scenario: Scenario ->
			bag = SsmCucumberBag.init(scenario)
		}

		Given("An admin") {
			runBlocking {
				bag.adminSigner = loadSignerAdmin()
			}
		}

		Given("The admin {string}") { adminName: AgentName ->

			runBlocking {
				bag.adminSigner = loadSignerAdmin(adminName)
			}
		}

		Given("The admin {string} with key {string}") { adminName: AgentName, key: String ->
			runBlocking {
				bag.adminSigner = loadSignerAdmin(adminName, key)
			}
		}

		Given("The user {string} with key {string}") { userName: AgentName, key: String ->
			runBlocking {
				bag.userSigners[userName] = loadSigner(userName, key)
			}
		}

		Given("A new user {string}") { userName: AgentName ->
			val contextUser = userName.contextualize(bag)
			bag.userSigners[contextUser] = SignerUser.generate(contextUser)
			runBlocking {
				registerUser(bag.userSigners.getValue(contextUser).asAgent())
			}
		}

		Given("A ssm {string} with configuration file {string}") { ssmName: SsmName, ssmConfigFile: String ->
			val contextSsm = ssmName.contextualize(bag)
			bag.ssms[contextSsm] = SsmJsonReader().readSsm(ssmConfigFile, contextSsm)
			runBlocking {
				createSsm(bag.ssms.getValue(contextSsm))
			}
		}

		Given("A ssm {string} with transitions") { ssmName: SsmName, table: DataTable ->
			val contextSsm = ssmName.contextualize(bag)
			bag.ssms[contextSsm] = Ssm(
				transitions = table.asTransitions(),
				name = contextSsm
			)
			runBlocking {
				createSsm(bag.ssms.getValue(contextSsm))
			}
		}

		Given("A session {string} started for {string}") { sessionName: SessionName, ssmName: SsmName ->
			val contextSsm = ssmName.contextualize(bag)
			runBlocking {
				val session = SsmSession(
					ssm = contextSsm,
					session = sessionName.contextualize(bag),
					roles = emptyMap(),
					public = "",
					private = emptyMap()
				)
				startSession(session)
			}
		}
		When("I register a user {string}") { userName: String ->
			runBlocking {
				val contextUser = userName.contextualize(bag)
				if(!bag.userSigners.containsKey(contextUser)){
					bag.userSigners[contextUser] = createUser(contextUser)
				}
				val user = bag.userSigners.getValue(contextUser).asAgent()
				registerUser(user)
			}
		}
		When("I create a ssm {string} with configuration file {string}") { ssmName: SsmName, ssmConfigFile: String ->
			val contextSsm = ssmName.contextualize(bag)
			bag.ssms[contextSsm] = SsmJsonReader().readSsm(ssmConfigFile, contextSsm)
			runBlocking {

				createSsm(bag.ssms.getValue(contextSsm))
			}
		}
		When("I create a ssm {string} with transitions") { ssmName: SsmName, table: DataTable ->
			val contextSsm = ssmName.contextualize(bag)
			bag.ssms[contextSsm] = Ssm(
				transitions = table.asTransitions(),
				name = contextSsm
			)
			runBlocking {
				createSsm(bag.ssms.getValue(contextSsm))
			}
		}
		When("I start a session {string} for {string}") { sessionName: SessionName, ssmName: SsmName, table: DataTable ->
			runBlocking {
				val contextSsm = ssmName.contextualize(bag)
				val session = SsmSession(
					ssm = contextSsm,
					session = sessionName.contextualize(bag),
					roles = table.asStartSessionRole(),
					public = "",
					private = null
				)
				startSession(session)
			}
		}
		When("I perform actions")
		{ table: DataTable ->
			runBlocking {
				table.asPerformAction().forEach { perform ->
					val contextUser = perform.userName
					val signer = bag.userSigners.getValue(contextUser)
					val context = SsmContext(
						session = perform.sessionName,
						public = perform.public,
						iteration = perform.iteration,
						private = emptyMap()
					)
					bag.clientTx(signer).sendPerform(bag.chaincodeUri, perform.action, context, signer.name)
				}
			}
		}
	}

	private fun DataTable.asStartSessionRole(): Map<String, AgentName> {
		return asMaps().associate { columns ->
			columns["userName"]!!.contextualize(bag) to columns["role"]!!
		}
	}

	private fun DataTable.asPerformAction(): List<PerformAction> {
		return asMaps().map { columns ->
			PerformAction(
				sessionName = columns[PerformAction::sessionName.name]!!.contextualize(bag),
				userName = columns[PerformAction::userName.name]!!.contextualize(bag),
				action = columns[PerformAction::action.name]!!,
				iteration = columns[PerformAction::iteration.name]?.toInt()!!,
				public = columns[PerformAction::public.name]!!,
				)
		}
	}

	data class PerformAction(
		val action: SsmAction,
		val iteration: Int,
		val sessionName: String,
		val userName: AgentName,
		val public: String
	)

	fun DataTable.asTransitions(): List<SsmTransition> {
		return asMaps().map { columns ->
			SsmTransition(
				from = columns[SsmTransition::from.name]?.toInt()!!,
				to = columns[SsmTransition::to.name]?.toInt()!!,
				role = columns[SsmTransition::role.name]!!,
				action = columns[SsmTransition::action.name]!!,
			)
		}
	}


	protected fun createUser(agentName: AgentName): SignerUser {
		return SignerUser.generate(agentName)
	}

	protected abstract suspend fun startSession(session: SsmSession)

	protected abstract suspend fun createSsm(ssm: Ssm)

	protected abstract suspend fun registerUser(agent: Agent)

	private fun loadSignerAdmin(adminName: AgentName? = null, filename: String? = null): SignerAdmin {
		val name = adminName ?: DEFAULT_ADMIN_NAME
		val keyFile = filename ?: DEFAULT_ADMIN_KEY
		return SignerAdmin.loadFromFile(name, keyFile)
	}

	protected abstract suspend fun loadSigner(agentName: AgentName, filename: String): SignerUser
}
