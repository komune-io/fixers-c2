package ssm.chaincode.f2.features.command

import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.f2.SsmTxTestFixtures
import ssm.chaincode.f2.StubSsmChaincodeRepository
import ssm.sdk.dsl.CommandOutcome
import ssm.tx.dsl.features.ssm.SsmCreateCommand

internal class SsmTxCreateFunctionImplTest {

	private fun function(repository: StubSsmChaincodeRepository) =
		SsmTxCreateFunctionImpl(SsmTxTestFixtures.txService(repository))

	private fun command(name: String = "CarDealership") = SsmCreateCommand(
		chaincodeUri = SsmTxTestFixtures.chaincodeUri,
		signerName = SsmTxTestFixtures.SIGNER_NAME,
		ssm = SsmTxTestFixtures.ssm(name),
	)

	@Test
	fun `sends a signed create invoke to the chaincode and maps the transactionId`() = runTest {
		val repository = StubSsmChaincodeRepository()

		val results = function(repository).invoke(flowOf(command())).toList()

		assertThat(repository.invoked).hasSize(1)
		val request = repository.invoked.single()
		assertThat(request.cmd).isEqualTo(InvokeRequestType.invoke)
		assertThat(request.fcn).isEqualTo("create")
		assertThat(request.channelid).isEqualTo(SsmTxTestFixtures.CHANNEL_ID)
		assertThat(request.chaincodeid).isEqualTo(SsmTxTestFixtures.CHAINCODE_ID)
		// args = [ssm json, signer name, signature]
		assertThat(request.args.toList()).hasSize(3)
		assertThat(request.args[0]).isEqualTo(SsmTxTestFixtures.json(SsmTxTestFixtures.ssm()))
		assertThat(request.args[1]).isEqualTo(SsmTxTestFixtures.SIGNER_NAME)
		assertThat(request.args[2]).isNotBlank()

		assertThat(results.map { it.transactionId }).isEqualTo(repository.invokedMsgIds.single().map { "tx-$it" })
	}

	@Test
	fun `batches several commands into a single invoke, in order`() = runTest {
		val repository = StubSsmChaincodeRepository()
		val names = listOf("ssm-A", "ssm-B", "ssm-C")

		val results = function(repository).invoke(flowOf(*names.map(::command).toTypedArray())).toList()

		assertThat(repository.invoked).hasSize(names.size)
		assertThat(repository.invokedMsgIds).hasSize(1)
		assertThat(repository.invoked.map { it.args[0] })
			.isEqualTo(names.map { SsmTxTestFixtures.json(SsmTxTestFixtures.ssm(it)) })
		assertThat(results).hasSize(names.size)
	}

	@Test
	fun `maps a missing transactionId to an empty string`() = runTest {
		val repository = StubSsmChaincodeRepository(onInvoke = { _, msgIds ->
			msgIds.map { CommandOutcome(outcome = "Rejected", msgId = it, errorCode = "MVCC") }
		})

		val results = function(repository).invoke(flowOf(command())).toList()

		assertThat(results.single().transactionId).isEmpty()
	}

	@Test
	fun `signs each command with the signer key so the signature differs per payload`() = runTest {
		val repository = StubSsmChaincodeRepository()

		function(repository).invoke(flowOf(command("ssm-A"), command("ssm-B"))).toList()

		val signatures = repository.invoked.map { it.args[2] }
		assertThat(signatures).doesNotHaveDuplicates()
	}
}
