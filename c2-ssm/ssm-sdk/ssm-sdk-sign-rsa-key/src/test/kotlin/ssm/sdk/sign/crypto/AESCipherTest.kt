package ssm.sdk.sign.crypto

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.NoSuchAlgorithmException
import java.util.Base64
import javax.crypto.SecretKey
import org.assertj.core.api.Assertions
import org.bouncycastle.crypto.CryptoException
import org.junit.jupiter.api.Test
import ssm.sdk.sign.FileUtils


internal class AESCipherTest {

	companion object {
		const val FILE_TO_COMMIT_TXT = "crypto/fileToCommit.txt"
	}

	private fun encryptToBytes(plainText: String, key: SecretKey): ByteArray {
		val output = ByteArrayOutputStream()
		AESCipher.encrypt(ByteArrayInputStream(plainText.toByteArray()), output, key)
		return output.toByteArray()
	}

	private fun decryptToString(payload: ByteArray, key: SecretKey): String {
		val stream: InputStream = AESCipher.decrypt(ByteArrayInputStream(payload), key)
		return stream.bufferedReader().use(BufferedReader::readText)
	}

	@Test
	@Throws(IOException::class, CryptoException::class)
	fun encryptThenDecryptFileRoundTrips() {
		val fileToEncrypt: File = FileUtils.getFile(FILE_TO_COMMIT_TXT)
		val key: SecretKey = AESCipher.generateSecretKey()
		val encrypted = ByteArrayOutputStream()

		AESCipher.encrypt(fileToEncrypt, encrypted, key)

		Assertions.assertThat(decryptToString(encrypted.toByteArray(), key)).isEqualTo("to commit")
	}

	@Test
	@Throws(CryptoException::class)
	fun encryptThenDecryptStreamRoundTrips() {
		val key: SecretKey = AESCipher.generateSecretKey()

		val encrypted = encryptToBytes("to commit", key)

		Assertions.assertThat(decryptToString(encrypted, key)).isEqualTo("to commit")
	}

	@Test
	fun encryptShouldProduceADifferentPayloadForTheSamePlainText() {
		val key: SecretKey = AESCipher.generateSecretKey()

		val first = encryptToBytes("to commit", key)
		val second = encryptToBytes("to commit", key)

		Assertions.assertThat(first).isNotEqualTo(second)
		Assertions.assertThat(decryptToString(first, key)).isEqualTo(decryptToString(second, key))
	}

	@Test
	fun decryptShouldRejectATamperedPayload() {
		val key: SecretKey = AESCipher.generateSecretKey()
		val encrypted = encryptToBytes("to commit", key)
		encrypted[encrypted.size - 1] = (encrypted[encrypted.size - 1] + 1).toByte()

		Assertions.assertThatThrownBy { decryptToString(encrypted, key) }
			.isInstanceOf(CryptoException::class.java)
	}

	@Test
	fun decryptShouldRejectAnotherKey() {
		val encrypted = encryptToBytes("to commit", AESCipher.generateSecretKey())

		Assertions.assertThatThrownBy { decryptToString(encrypted, AESCipher.generateSecretKey()) }
			.isInstanceOf(CryptoException::class.java)
	}

	@Test
	@Throws(NoSuchAlgorithmException::class)
	fun generateSecretKeyShouldNotBeDeterministic() {
		val keys = (1..5).map { Base64.getEncoder().encodeToString(AESCipher.generateSecretKey().encoded) }

		Assertions.assertThat(keys).doesNotHaveDuplicates()
	}

	@Test
	@Throws(NoSuchAlgorithmException::class)
	fun generateSecretKeyRoundTripsThroughBase64() {
		val key: SecretKey = AESCipher.generateSecretKey()
		val encodedKey = Base64.getEncoder().encodeToString(key.encoded)

		val keyBuilt: SecretKey = AESCipher.secretKeyFromBase64(encodedKey)

		Assertions.assertThat(key.algorithm).isEqualTo(keyBuilt.algorithm)
		Assertions.assertThat(key.encoded).isEqualTo(keyBuilt.encoded)
		Assertions.assertThat(key.format).isEqualTo(keyBuilt.format)
	}
}
