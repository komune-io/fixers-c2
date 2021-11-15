package ssm.sdk.client.domain

import java.util.UUID
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.SsmContext
import ssm.sdk.client.SsmClientItTest
import ssm.sdk.client.extention.addPrivateMessage
import ssm.sdk.client.extention.getPrivateMessage
import ssm.sdk.client.extention.loadFromFile
import ssm.sdk.sign.crypto.KeyPairReader.loadPrivateKey
import ssm.sdk.sign.model.SignerUser

internal class HasPrivateMessageTest {
	@Test
	@Throws(Exception::class)
	fun shouldEncryptMessage() {
		val sessionName = "deal20181201-" + UUID.randomUUID().toString()
		var context = SsmContext(sessionName, "100 dollars 1978 Camaro", 0, HashMap())
		val agent = loadFromFile(SsmClientItTest.USER1_NAME, SsmClientItTest.USER1_FILENAME)
		context = context.addPrivateMessage("Value to encrypt", agent)
		val `val` = context.private!![agent.name]
		Assertions.assertThat(`val`).isNotEmpty.isNotEqualTo("Value to encrypt")
	}

	@Test
	@Throws(Exception::class)
	fun shouldDecryptMessage() {
		val sessionName = "deal20181201-" + UUID.randomUUID().toString()
		var context = SsmContext(sessionName, "100 dollars 1978 Camaro", 0, HashMap())
		val agent = loadFromFile(SsmClientItTest.USER1_NAME, SsmClientItTest.USER1_FILENAME)
		context = context.addPrivateMessage("Value to encrypt", agent)
		val privKey = loadPrivateKey(SsmClientItTest.USER1_FILENAME)
		val `val` = context.getPrivateMessage(SsmClientItTest.USER1_NAME, privKey)
		Assertions.assertThat(`val`).isNotEmpty.isEqualTo("Value to encrypt")
	}

	@Test
	@Throws(Exception::class)
	fun shouldDecryptMessageWithSigner() {
		val sessionName = "deal20181201-" + UUID.randomUUID().toString()
		var context = SsmContext(sessionName, "100 dollars 1978 Camaro", 0, HashMap())
		val agent = loadFromFile(SsmClientItTest.USER1_NAME, SsmClientItTest.USER1_FILENAME)
		context = context.addPrivateMessage("Value to encrypt", agent)
		val signerUser1 = SignerUser.loadFromFile(SsmClientItTest.USER1_NAME, SsmClientItTest.USER1_FILENAME)
		val `val` = context.getPrivateMessage(signerUser1)
		Assertions.assertThat(`val`).isNotEmpty.isEqualTo("Value to encrypt")
	}
}
