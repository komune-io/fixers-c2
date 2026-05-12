package ssm.chaincode.bdd

import f2.dsl.fnc.invoke
import io.cucumber.java8.En
import io.cucumber.java8.Scenario
import ssm.bdd.config.SsmQueryStep
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionQuery
import ssm.chaincode.dsl.query.SsmListAdminQuery
import ssm.chaincode.dsl.query.SsmListSessionQuery
import ssm.chaincode.dsl.query.SsmListSsmQuery
import ssm.chaincode.dsl.query.SsmListUserQuery
import ssm.chaincode.f2.ChaincodeSsmQueriesImpl

class SsmChaincodeBddSteps : SsmQueryStep(), En {
	companion object {
		const val GLUE = "ssm.chaincode.bdd"
	}
	init {
		prepareSteps()
		Before { _: Scenario ->
			ssmChaincodeQueryFunctions = ChaincodeSsmQueriesImpl(batch, config)
		}

	}
	lateinit var ssmChaincodeQueryFunctions: ChaincodeSsmQueriesImpl

	override suspend fun getSession(ssmUri: SsmUri, sessionName: String): SsmSessionState? {
		return ssmChaincodeQueryFunctions.ssmGetSessionQueryFunction()
			.invoke(SsmGetSessionQuery(chaincodeUri = ssmUri.chaincodeUri, sessionName = sessionName))
			.item
	}

	override suspend fun logSession(ssmUri: SsmUri, sessionName: String): List<SsmSessionStateLog> {
		return ssmChaincodeQueryFunctions.ssmGetSessionLogsQueryFunction()
			.invoke(SsmGetSessionLogsQuery(
				chaincodeUri = ssmUri.chaincodeUri,
				ssmName = ssmUri.ssmName,
				sessionName = sessionName)
			)
			.logs
	}

	override suspend fun listSessions(ssmUri: SsmUri): List<String> {
		return ssmChaincodeQueryFunctions.ssmListSessionQueryFunction()
			.invoke(SsmListSessionQuery(bag.chaincodeUri))
			.items.toList()
	}

	override suspend fun listSsm(): List<String> {
		return ssmChaincodeQueryFunctions.ssmListSsmQueryFunction()
			.invoke(SsmListSsmQuery(bag.chaincodeUri))
			.items.toList()
	}

	override suspend fun listUsers(): List<String> =
		ssmChaincodeQueryFunctions.ssmListUserQueryFunction()
			.invoke(SsmListUserQuery(bag.chaincodeUri))
			.items.toList()

	override suspend fun listAdmins(): List<String> =
		ssmChaincodeQueryFunctions.ssmListAdminQueryFunction()
			.invoke(SsmListAdminQuery(bag.chaincodeUri))
			.items.toList()
}
