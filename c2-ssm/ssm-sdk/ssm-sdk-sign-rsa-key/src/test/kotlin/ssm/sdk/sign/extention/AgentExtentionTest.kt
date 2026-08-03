package ssm.sdk.sign.extention

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent
import ssm.sdk.sign.crypto.KeyPairReader

internal class AgentExtentionTest {

	companion object {
		private const val KEY_NAME = "command/vivi"
	}

	@Test
	fun loadFromFileWithNameShouldLoadPublicKey() {
		val agent = Agent.loadFromFile(KEY_NAME)
		val expected = KeyPairReader.loadPublicKey(KEY_NAME)
		Assertions.assertThat(agent.name).isEqualTo(KEY_NAME)
		Assertions.assertThat(agent.pub).isEqualTo(expected.encoded)
	}

	@Test
	fun loadFromFileWithNameAndFilenameShouldLoadPublicKey() {
		val agent = Agent.loadFromFile("vivi", KEY_NAME)
		val expected = KeyPairReader.loadPublicKey(KEY_NAME)
		Assertions.assertThat(agent.name).isEqualTo("vivi")
		Assertions.assertThat(agent.pub).isEqualTo(expected.encoded)
	}

	@Test
	fun getPubAsKeyShouldRebuildPublicKey() {
		val agent = Agent.loadFromFile("vivi", KEY_NAME)
		val expected = KeyPairReader.loadPublicKey(KEY_NAME)
		Assertions.assertThat(agent.getPubAsKey().encoded).isEqualTo(expected.encoded)
	}
}
