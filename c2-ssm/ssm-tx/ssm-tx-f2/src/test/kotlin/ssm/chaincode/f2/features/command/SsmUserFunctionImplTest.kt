package ssm.chaincode.f2.features.command

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.f2.SsmTxTestFixtures
import ssm.chaincode.f2.StubSsmChaincodeRepository
import ssm.sdk.dsl.CommandOutcome
import ssm.tx.dsl.features.user.SsmUserGrantCommand
import ssm.tx.dsl.features.user.SsmUserRegisterCommand

/** Register and grant share the `register` chaincode command; both mappings are pinned here. */
internal class SsmUserFunctionImplTest {

	private fun registerCommand(name: String = "bob") = SsmUserRegisterCommand(
		chaincodeUri = SsmTxTestFixtures.chaincodeUri,
		signerName = SsmTxTestFixtures.SIGNER_NAME,
		agent = SsmTxTestFixtures.agent(name),
	)

	private fun grantCommand(name: String = "bob") = SsmUserGrantCommand(
		chaincodeUri = SsmTxTestFixtures.chaincodeUri,
		signerName = SsmTxTestFixtures.SIGNER_NAME,
		agent = SsmTxTestFixtures.agent(name),
	)

	@Test
	suspend fun `register sends a signed register invoke and maps the transactionId`() {
		val repository = StubSsmChaincodeRepository()
		val function = SsmUserRegisterFunctionImpl(SsmTxTestFixtures.txService(repository))

		val results = function.invoke(flowOf(registerCommand())).toList()

		val request = repository.invoked.single()
		assertThat(request.fcn).isEqualTo("register")
		assertThat(request.channelid).isEqualTo(SsmTxTestFixtures.CHANNEL_ID)
		assertThat(request.args[0]).isEqualTo(SsmTxTestFixtures.json(SsmTxTestFixtures.agent()))
		assertThat(request.args[1]).isEqualTo(SsmTxTestFixtures.SIGNER_NAME)
		assertThat(request.args[2]).isNotBlank()
		assertThat(results.single().transactionId).startsWith("tx-")
	}

	@Test
	suspend fun `grant sends a signed register invoke and maps the transactionId`() {
		val repository = StubSsmChaincodeRepository()
		val function = SsmUserGrantFunctionImpl(SsmTxTestFixtures.txService(repository))

		val results = function.invoke(flowOf(grantCommand())).toList()

		val request = repository.invoked.single()
		assertThat(request.fcn).isEqualTo("register")
		assertThat(request.args[0]).isEqualTo(SsmTxTestFixtures.json(SsmTxTestFixtures.agent()))
		assertThat(results.single().transactionId).startsWith("tx-")
	}

	@Test
	suspend fun `register batches several agents into a single invoke call`() {
		val repository = StubSsmChaincodeRepository()
		val function = SsmUserRegisterFunctionImpl(SsmTxTestFixtures.txService(repository))
		val names = listOf("bob", "alice")

		val results = function.invoke(flowOf(*names.map(::registerCommand).toTypedArray())).toList()

		assertThat(repository.invokedMsgIds).hasSize(1)
		assertThat(repository.invoked.map { it.args[0] })
			.isEqualTo(names.map { SsmTxTestFixtures.json(SsmTxTestFixtures.agent(it)) })
		assertThat(results).hasSize(names.size)
	}

	@Test
	suspend fun `register maps a missing transactionId to an empty string`() {
		val repository = StubSsmChaincodeRepository(onInvoke = { _, msgIds ->
			msgIds.map { CommandOutcome(outcome = "Indeterminate", msgId = it) }
		})
		val function = SsmUserRegisterFunctionImpl(SsmTxTestFixtures.txService(repository))

		assertThat(function.invoke(flowOf(registerCommand())).toList().single().transactionId).isEmpty()
	}

	@Test
	suspend fun `grant maps a missing transactionId to an empty string`() {
		val repository = StubSsmChaincodeRepository(onInvoke = { _, msgIds ->
			msgIds.map { CommandOutcome(outcome = "Indeterminate", msgId = it) }
		})
		val function = SsmUserGrantFunctionImpl(SsmTxTestFixtures.txService(repository))

		assertThat(function.invoke(flowOf(grantCommand())).toList().single().transactionId).isEmpty()
	}
}
