package ssm.sdk.sign

import io.komune.c2.chaincode.dsl.ChaincodeUri
import java.security.Signature
import java.util.Base64
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.sdk.dsl.SsmCmd
import ssm.sdk.dsl.SsmCmdName
import ssm.sdk.sign.crypto.KeyPairReader
import ssm.sdk.sign.crypto.Sha256RSASigner
import ssm.sdk.sign.exception.SsmSignException
import ssm.sdk.sign.extention.asAgent
import ssm.sdk.sign.model.SignerUser

internal class SsmCmdSignerSha256RSASignerTest {

	companion object {
		private const val KEY_NAME = "command/vivi"
		private const val AGENT_NAME = "vivi"
	}

	private val keyPair = KeyPairReader.loadKeyPair(KEY_NAME)
	private val signerUser = SignerUser(AGENT_NAME, keyPair)

	private fun cmd(agentName: String = AGENT_NAME) = SsmCmd(
		chaincodeUri = ChaincodeUri("chaincode:sandbox:ssm"),
		agentName = agentName,
		json = """{"session":"session"}""",
		command = SsmCmdName.PERFORM,
		performAction = "confirm",
		valueToSign = "value-to-sign"
	)

	@Test
	fun signShouldProduceVerifiableSignature() {
		val signed = SsmCmdSignerSha256RSASigner(signerUser).sign(cmd())

		Assertions.assertThat(signed.signer).isEqualTo(AGENT_NAME)
		Assertions.assertThat(signed.cmd).isEqualTo(cmd())
		Assertions.assertThat(signed.chaincodeUri).isEqualTo(ChaincodeUri("chaincode:sandbox:ssm"))

		val verifier = Signature.getInstance("SHA256withRSA")
		verifier.initVerify(keyPair.public)
		verifier.update("value-to-sign".toByteArray(Charsets.UTF_8))
		val valid = verifier.verify(Base64.getDecoder().decode(signed.signature))
		Assertions.assertThat(valid).isTrue()
	}

	@Test
	fun signShouldFailForUnknownAgent() {
		val signer = SsmCmdSignerSha256RSASigner(signerUser)
		Assertions.assertThatThrownBy { signer.sign(cmd(agentName = "unknown")) }
			.isInstanceOf(SsmSignException::class.java)
	}

	@Test
	fun signShouldReportOnlyTheAgentNameOnFailure() {
		val signer = SsmCmdSignerSha256RSASigner(signerUser)
		Assertions.assertThatThrownBy { signer.sign(cmd(agentName = "unknown")) }
			.isInstanceOf(SsmSignException::class.java)
			.hasMessage("Invalid agent name: unknown")
	}

	@Test
	fun rsaSignAsB64ShouldMatchRsaSign() {
		val raw = Sha256RSASigner.rsaSign("payload", keyPair.private)
		val b64 = Sha256RSASigner.rsaSignAsB64("payload", keyPair.private)
		Assertions.assertThat(Base64.getDecoder().decode(b64)).isEqualTo(raw)
	}

	@Test
	fun asAgentShouldExposePublicKey() {
		val agent = signerUser.asAgent()
		Assertions.assertThat(agent.name).isEqualTo(AGENT_NAME)
		Assertions.assertThat(agent.pub).isEqualTo(keyPair.public.encoded)
	}
}
