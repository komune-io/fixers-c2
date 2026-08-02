package ssm.sdk.sign.extention

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.SsmContext
import ssm.chaincode.dsl.model.SsmSession
import ssm.chaincode.dsl.model.SsmSessionState
import ssm.sdk.sign.crypto.KeyPairReader
import ssm.sdk.sign.model.SignerUser

internal class PrivateMessageExtentionTest {

	companion object {
		private const val KEY_NAME = "command/vivi"
		private const val AGENT_NAME = "vivi"
		private const val MESSAGE = "very private message"
	}

	private val keyPair = KeyPairReader.loadKeyPair(KEY_NAME)

	private fun context() = SsmContext(
		session = "session",
		public = "public",
		iteration = 0,
		private = null
	)

	@Test
	fun ssmContextPrivateMessageRoundTrip() {
		val context = context().addPrivateMessage(MESSAGE, AGENT_NAME, keyPair.public)
		Assertions.assertThat(context.private).containsKey(AGENT_NAME)
		val decrypted = context.getPrivateMessage(AGENT_NAME, keyPair.private)
		Assertions.assertThat(decrypted).isEqualTo(MESSAGE)
	}

	@Test
	fun ssmContextPrivateMessageWithAgentRoundTrip() {
		val agent = Agent.loadFromFile(AGENT_NAME, KEY_NAME)
		val context = context().addPrivateMessage(MESSAGE, agent)
		val decrypted = context.getPrivateMessage(AGENT_NAME, keyPair.private)
		Assertions.assertThat(decrypted).isEqualTo(MESSAGE)
	}

	@Test
	fun ssmContextPrivateMessageWithSignerRoundTrip() {
		val signer = SignerUser(AGENT_NAME, keyPair)
		val context = context().addPrivateMessage(MESSAGE, AGENT_NAME, keyPair.public)
		Assertions.assertThat(context.getPrivateMessage(signer)).isEqualTo(MESSAGE)
	}

	@Test
	fun getPrivateMessageShouldReturnNullWhenAgentIsUnknown() {
		val context = context().addPrivateMessage(MESSAGE, AGENT_NAME, keyPair.public)
		Assertions.assertThat(context.getPrivateMessage("unknown", keyPair.private)).isNull()
	}

	@Test
	fun getPrivateMessageShouldReturnNullWhenPrivateIsNull() {
		Assertions.assertThat(context().getPrivateMessage(AGENT_NAME, keyPair.private)).isNull()
	}

	@Test
	fun addPrivateMessageShouldPreserveExistingEntries() {
		val otherKeyPair = KeyPairReader.generateRSAKey()
		val context = context()
			.addPrivateMessage(MESSAGE, AGENT_NAME, keyPair.public)
			.addPrivateMessage("other message", "other", otherKeyPair.public)
		Assertions.assertThat(context.private).containsOnlyKeys(AGENT_NAME, "other")
		Assertions.assertThat(context.getPrivateMessage(AGENT_NAME, keyPair.private)).isEqualTo(MESSAGE)
		Assertions.assertThat(context.getPrivateMessage("other", otherKeyPair.private)).isEqualTo("other message")
	}

	@Test
	fun ssmSessionPrivateMessageRoundTrip() {
		val session = SsmSession(
			ssm = "ssm",
			session = "session",
			roles = emptyMap(),
			public = "public",
			private = null
		).addPrivateMessage(MESSAGE, AGENT_NAME, keyPair.public)
		Assertions.assertThat(session.private).containsKey(AGENT_NAME)
		Assertions.assertThat(session.getPrivateMessage(AGENT_NAME, keyPair.private)).isEqualTo(MESSAGE)
	}

	@Test
	fun ssmSessionStatePrivateMessageRoundTrip() {
		val state = SsmSessionState(
			ssm = "ssm",
			session = "session",
			roles = emptyMap(),
			public = "public",
			private = null,
			origin = null,
			current = 0,
			iteration = 0
	).addPrivateMessage(MESSAGE, AGENT_NAME, keyPair.public)
		Assertions.assertThat(state.private).containsKey(AGENT_NAME)
		Assertions.assertThat(state.getPrivateMessage(AGENT_NAME, keyPair.private)).isEqualTo(MESSAGE)
	}
}
