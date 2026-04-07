package ssm.api.features.query

import f2.dsl.fnc.invoke
import f2.dsl.fnc.invokeWith
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.api.DataSsmQueryFunctionImpl
import ssm.bdd.config.SsmBddConfig
import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmTransition
import ssm.data.dsl.features.query.DataSsmListQuery
import ssm.data.dsl.features.query.DataSsmListQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQuery
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction
import ssm.sdk.core.SsmSdkConfig
import ssm.sdk.core.SsmServiceFactory
import ssm.sdk.sign.SsmCmdSignerSha256RSASigner
import ssm.sdk.sign.extention.asAgent
import ssm.sdk.sign.model.SignerAdmin
import ssm.sdk.sign.model.SignerUser

internal class DataSsmSessionListQueryFunctionImplTest {

	private val chaincodeUri = SsmBddConfig.Chaincode.chaincodeUri

	private val dataSsmQueryFunction = DataSsmQueryFunctionImpl(
		SsmBddConfig.Data.config,
	)

	private val function: DataSsmSessionListQueryFunction = dataSsmQueryFunction.dataSsmSessionListQueryFunction()
	private val dataSsmListQueryFunction: DataSsmListQueryFunction = dataSsmQueryFunction.dataSsmListQueryFunction()


	@Test
	fun `test exception`() = runTest {
		val uuid = UUID.randomUUID().toString().take(8)
		val createdSsmName = createSsmWithSession(uuid)

		val ssmListResult = dataSsmListQueryFunction.invoke(
			DataSsmListQuery(listOf(chaincodeUri))
		)
		val createdSsmUri = ssmListResult.items
			.first { it.ssm.name == createdSsmName }
			.uri
		val result = DataSsmSessionListQuery(
			ssmUri = createdSsmUri,
		).invokeWith(function)
		Assertions.assertThat(result.items).isNotEmpty()
	}

	private suspend fun createSsmWithSession(uuid: String): String {
		val (adminName, adminPath) = SsmBddConfig.Key.admin
		val admin = SignerAdmin.loadFromFile(adminName, adminPath)
		val factory = SsmServiceFactory.builder(
			SsmSdkConfig(SsmBddConfig.Chaincode.url),
			SsmBatchProperties()
		)
		val txService = factory.buildTxService(SsmCmdSignerSha256RSASigner(admin))

		val ssmName = "data-test-ssm-$uuid"
		val ssm = Ssm(
			name = ssmName,
			transitions = listOf(
				SsmTransition(from = 0, to = 1, role = "Tester", action = "Test")
			)
		)
		txService.sendCreate(chaincodeUri, ssm, admin.name)

		val user = SignerUser.generate("data-test-user-$uuid")
		txService.sendRegisterUser(chaincodeUri, user.asAgent(), admin.name)

		val session = SsmSession(
			ssm = ssmName,
			session = "data-test-session-$uuid",
			roles = mapOf(user.name to "Tester"),
			public = "",
			private = null
		)
		txService.sendStart(chaincodeUri, session, admin.name)
		return ssmName
	}
}
