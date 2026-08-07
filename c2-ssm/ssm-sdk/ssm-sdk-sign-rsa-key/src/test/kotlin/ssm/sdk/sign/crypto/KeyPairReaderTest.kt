package ssm.sdk.sign.crypto

import java.io.StringReader
import java.security.interfaces.RSAPublicKey
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ssm.sdk.sign.FileUtils

internal class KeyPairReaderTest {

	companion object {
		private const val KEY_NAME = "command/vivi"
	}

	@Test
	fun loadKeyPairFromFilename() {
		val keyPair = KeyPairReader.loadKeyPair(KEY_NAME)
		Assertions.assertThat(keyPair.public.algorithm).isEqualTo("RSA")
		Assertions.assertThat(keyPair.private.algorithm).isEqualTo("RSA")
	}

	@Test
	fun loadKeyPairFromStrings() {
		val pubStr = FileUtils.getReader("$KEY_NAME.pub").use { it.readText() }
		val prvStr = FileUtils.getReader(KEY_NAME).use { it.readText() }
		val keyPair = KeyPairReader.loadKeyPair(pubStr, prvStr)
		val expected = KeyPairReader.loadKeyPair(KEY_NAME)
		Assertions.assertThat(keyPair.public.encoded).isEqualTo(expected.public.encoded)
		Assertions.assertThat(keyPair.private.encoded).isEqualTo(expected.private.encoded)
	}

	@Test
	fun loadPublicKeyShouldAppendPubExtension() {
		val fromName = KeyPairReader.loadPublicKey(KEY_NAME)
		val fromFile = KeyPairReader.loadPublicKey("$KEY_NAME.pub")
		Assertions.assertThat(fromName.encoded).isEqualTo(fromFile.encoded)
	}

	@Test
	fun fromByteArrayShouldRebuildPublicKey() {
		val pub = KeyPairReader.loadPublicKey(KEY_NAME)
		val rebuilt = KeyPairReader.fromByteArray(pub.encoded)
		Assertions.assertThat(rebuilt.encoded).isEqualTo(pub.encoded)
		Assertions.assertThat(rebuilt.algorithm).isEqualTo("RSA")
	}

	@Test
	fun generateRSAKeyShouldProduce2048BitsKey() {
		val keyPair = KeyPairReader.generateRSAKey()
		val publicKey = keyPair.public as RSAPublicKey
		Assertions.assertThat(publicKey.modulus.bitLength()).isEqualTo(2048)
		Assertions.assertThat(keyPair.private.algorithm).isEqualTo("RSA")
	}

	@Test
	fun loadPrivateKeyShouldFailOnInvalidContent() {
		Assertions.assertThatThrownBy {
			KeyPairReader.loadPrivateKey(StringReader("not a pem content"))
		}.isInstanceOf(Exception::class.java)
	}

	@Test
	fun loadPublicKeyShouldFailOnMissingFile() {
		Assertions.assertThatThrownBy {
			KeyPairReader.loadPublicKey("command/unknown-agent")
		}.isInstanceOf(Exception::class.java)
	}
}
