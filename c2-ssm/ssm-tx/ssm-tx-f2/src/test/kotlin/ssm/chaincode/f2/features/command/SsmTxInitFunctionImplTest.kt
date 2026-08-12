package ssm.chaincode.f2.features.command

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.f2.SsmTxTestFixtures
import ssm.chaincode.f2.StubSsmChaincodeRepository
import ssm.chaincode.f2.StubSsmChaincodeRepository.Companion.key
import ssm.chaincode.f2.utils.SsmException
import ssm.tx.dsl.features.ssm.SsmInitCommand

/**
 * Covers the create-if-not-exist logic of the init function, including the concurrent-creation
 * fallback, which the sandbox-bound BDD suite never exercised deterministically.
 */
internal class SsmTxInitFunctionImplTest {

	private val agent = SsmTxTestFixtures.agent()
	private val ssm = SsmTxTestFixtures.ssm()

	private val command = SsmInitCommand(
		chaincodeUri = SsmTxTestFixtures.chaincodeUri,
		signerName = SsmTxTestFixtures.SIGNER_NAME,
		ssm = ssm,
		agent = agent,
	)

	private fun function(repository: StubSsmChaincodeRepository) = SsmTxInitFunctionImpl(
		SsmTxTestFixtures.txService(repository),
		SsmTxTestFixtures.queryService(repository),
	)

	private fun agentFound(times: Int = 1) =
		key("user", agent.name) to List(times) { SsmTxTestFixtures.json(agent) }

	private fun ssmFound(times: Int = 1) =
		key("ssm", ssm.name) to List(times) { SsmTxTestFixtures.json(ssm) }

	@Test
	suspend fun `registers the agent and creates the ssm when neither exists`() {
		val repository = StubSsmChaincodeRepository()

		val results = function(repository).invoke(flowOf(command)).toList()

		assertThat(repository.invoked.map { it.fcn }).containsExactly("register", "create")
		assertThat(results.single().results).hasSize(2)
		assertThat(results.single().results).allSatisfy { assertThat(it).startsWith("tx-") }
	}

	@Test
	suspend fun `invokes nothing when both the agent and the ssm already exist`() {
		val repository = StubSsmChaincodeRepository(mapOf(agentFound(), ssmFound()))

		val results = function(repository).invoke(flowOf(command)).toList()

		assertThat(repository.invoked).isEmpty()
		assertThat(results.single().results).isEmpty()
		assertThat(repository.queries.map { it.fcn }).containsExactly("user", "ssm")
		assertThat(repository.queries.map { it.channelId }).containsOnly(SsmTxTestFixtures.CHANNEL_ID)
	}

	@Test
	suspend fun `only creates the ssm when the agent already exists`() {
		val repository = StubSsmChaincodeRepository(mapOf(agentFound()))

		val results = function(repository).invoke(flowOf(command)).toList()

		assertThat(repository.invoked.map { it.fcn }).containsExactly("create")
		assertThat(results.single().results).hasSize(1)
	}

	@Test
	suspend fun `swallows a creation failure when a concurrent writer created the agent meanwhile`() {
		// The agent is absent on the first check, the register invoke fails, and the re-check finds it.
		val repository = StubSsmChaincodeRepository(
			responses = mapOf(
				key("user", agent.name) to listOf(
					StubSsmChaincodeRepository.NOT_FOUND,
					SsmTxTestFixtures.json(agent),
				),
				ssmFound(),
			),
			onInvoke = { _, _ -> error("gateway unavailable") },
		)

		val results = function(repository).invoke(flowOf(command)).toList()

		assertThat(results.single().results).isEmpty()
	}

	@Test
	suspend fun `wraps a creation failure in SsmException when the agent is still missing`() {
		val repository = StubSsmChaincodeRepository(onInvoke = { _, _ -> error("gateway unavailable") })

		val thrown = runCatching { function(repository).invoke(flowOf(command)).toList() }.exceptionOrNull()

		assertThat(thrown).isInstanceOf(SsmException::class.java)
		assertThat(repository.invoked.map { it.fcn }).containsExactly("register")
	}

	@Test
	suspend fun `wraps a ssm creation failure in SsmException when the ssm is still missing`() {
		val repository = StubSsmChaincodeRepository(
			responses = mapOf(agentFound()),
			onInvoke = { _, _ -> error("gateway unavailable") },
		)

		val thrown = runCatching { function(repository).invoke(flowOf(command)).toList() }.exceptionOrNull()

		assertThat(thrown).isInstanceOf(SsmException::class.java)
		assertThat(repository.invoked.map { it.fcn }).containsExactly("create")
	}
}
