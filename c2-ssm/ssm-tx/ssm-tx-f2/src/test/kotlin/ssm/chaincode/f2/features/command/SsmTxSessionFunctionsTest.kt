package ssm.chaincode.f2.features.command

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.f2.SsmTxTestFixtures
import ssm.chaincode.f2.StubSsmChaincodeRepository
import ssm.sdk.core.command.SsmPerformCommand
import ssm.sdk.core.command.SsmStartCommand
import ssm.sdk.dsl.CommandOutcome

internal class SsmTxSessionFunctionsTest {

	private fun session(msgId: String) = SsmSession(
		ssm = "CarDealership",
		session = "session-$msgId",
		roles = mapOf(SsmTxTestFixtures.SIGNER_NAME to "Seller"),
		public = "{}",
		private = mapOf(),
	)

	private fun startCommand(msgId: String, timestamp: Long? = null) = SsmStartCommand(
		msgId = msgId,
		session = session(msgId),
		chaincodeUri = SsmTxTestFixtures.chaincodeUri,
		signerName = SsmTxTestFixtures.SIGNER_NAME,
		timestamp = timestamp,
	)

	private fun performCommand(msgId: String, timestamp: Long? = null) = SsmPerformCommand(
		msgId = msgId,
		action = "Sell",
		context = SsmContext(session = "session-$msgId", public = "{}", iteration = 1, private = mapOf()),
		chaincodeUri = SsmTxTestFixtures.chaincodeUri,
		signerName = SsmTxTestFixtures.SIGNER_NAME,
		timestamp = timestamp,
	)

	@Test
	fun `start sends a signed start invoke keyed by the command msgId`() = runTest {
		val repository = StubSsmChaincodeRepository()
		val function = SsmTxSessionStartFunctionImpl(SsmTxTestFixtures.txService(repository))

		val outcomes = function.invoke(flowOf(startCommand("start-a"), startCommand("start-b"))).toList()

		assertThat(repository.invoked.map { it.fcn }).containsOnly("start")
		assertThat(repository.invokedMsgIds.single()).containsExactly("start-a", "start-b")
		assertThat(outcomes.map { it.msgId }).containsExactly("start-a", "start-b")
	}

	@Test
	fun `perform sends the action as the first invoke argument`() = runTest {
		val repository = StubSsmChaincodeRepository()
		val function = SsmTxSessionPerformActionFunctionImpl(SsmTxTestFixtures.txService(repository))

		val outcomes = function.invoke(flowOf(performCommand("perform-a"))).toList()

		val request = repository.invoked.single()
		assertThat(request.fcn).isEqualTo("perform")
		// args = [action, context json, signer name, signature]
		assertThat(request.args).hasSize(4)
		assertThat(request.args[0]).isEqualTo("Sell")
		assertThat(request.args[2]).isEqualTo(SsmTxTestFixtures.SIGNER_NAME)
		assertThat(outcomes.single().msgId).isEqualTo("perform-a")
	}

	@Test
	fun `business timestamp is carried as transport metadata, never as an on-chain argument`() = runTest {
		val repository = StubSsmChaincodeRepository()
		val function = SsmTxSessionStartFunctionImpl(SsmTxTestFixtures.txService(repository))

		function.invoke(flowOf(startCommand("with-ts", timestamp = 1_623_715_200_000L), startCommand("no-ts")))
			.toList()

		assertThat(repository.invoked[0].timestamp).isEqualTo(1_623_715_200_000L)
		assertThat(repository.invoked[1].timestamp).isNull()
		assertThat(repository.invoked[0].args.toList()).doesNotContain("1623715200000")
	}

	@Test
	fun `outcomes are returned verbatim, including failures`() = runTest {
		val repository = StubSsmChaincodeRepository(onInvoke = { _, msgIds ->
			msgIds.map { CommandOutcome(outcome = "Conflict", msgId = it, errorCode = "MVCC_READ_CONFLICT") }
		})
		val function = SsmTxSessionPerformActionFunctionImpl(SsmTxTestFixtures.txService(repository))

		val outcomes = function.invoke(flowOf(performCommand("perform-a"))).toList()

		assertThat(outcomes.single().outcome).isEqualTo("Conflict")
		assertThat(outcomes.single().errorCode).isEqualTo("MVCC_READ_CONFLICT")
	}

	@Test
	fun `a command signed by an unknown agent is rejected without aborting the batch`() = runTest {
		val repository = StubSsmChaincodeRepository()
		val function = SsmTxSessionStartFunctionImpl(SsmTxTestFixtures.txService(repository))
		val unsignable = startCommand("unsignable").copy(signerName = "not-a-known-signer")

		val outcomes = function.invoke(flowOf(unsignable, startCommand("signable"))).toList()

		assertThat(repository.invoked.map { it.args[1] }).containsOnly(SsmTxTestFixtures.SIGNER_NAME)
		val byMsgId = outcomes.associateBy { it.msgId }
		assertThat(byMsgId.getValue("unsignable").outcome).isEqualTo("Rejected")
		assertThat(byMsgId.getValue("unsignable").errorCode).isEqualTo("SIGN_FAILED")
		assertThat(byMsgId.getValue("signable").outcome).isEqualTo("Committed")
	}
}
