package ssm.sdk.sign.crypto

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.NoSuchAlgorithmException
import java.util.Base64
import javax.crypto.SecretKey
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.bouncycastle.crypto.CryptoException
import org.junit.jupiter.api.Test
import ssm.sdk.sign.FileUtils

internal class AESCipherTest {

	companion object {
		const val FILE_TO_COMMIT_TXT = "crypto/fileToCommit.txt"
		const val KEY_B64 = "+cRaRuaSK1/RObE9oEOm6Q=="
	}

	@Test
	@Throws(IOException::class, CryptoException::class)
	fun encryptThenDecryptFileRestoresContent() {
		val fileToEncrypt: File = FileUtils.getFile(FILE_TO_COMMIT_TXT)
		val encryptedFile = File.createTempFile("enc_", "tmp")
		try {
			val key: SecretKey = AESCipher.secretKeyFromBase64(KEY_B64)
			FileOutputStream(encryptedFile).use { os ->
				AESCipher.encrypt(fileToEncrypt, os, key)
			}
			Assertions.assertThat(FileUtils.sameContent(fileToEncrypt.toPath(), encryptedFile.toPath())).isFalse
			val decryptedStream: InputStream = AESCipher.decrypt(FileInputStream(encryptedFile), key)
			val value = decryptedStream.bufferedReader().use(BufferedReader::readText)
			Assertions.assertThat(value).isEqualTo("to commit")
		} finally {
			encryptedFile.delete()
		}
	}

	@Test
	@Throws(CryptoException::class)
	fun encryptProducesDifferentCiphertextForEachCall() {
		val key: SecretKey = AESCipher.secretKeyFromBase64(KEY_B64)
		val first = ByteArrayOutputStream()
		val second = ByteArrayOutputStream()
		AESCipher.encrypt(ByteArrayInputStream("payload".toByteArray()), first, key)
		AESCipher.encrypt(ByteArrayInputStream("payload".toByteArray()), second, key)
		Assertions.assertThat(first.toByteArray()).isNotEqualTo(second.toByteArray())
	}

	@Test
	@Throws(CryptoException::class)
	fun decryptWithWrongKeyFails() {
		val key: SecretKey = AESCipher.secretKeyFromBase64(KEY_B64)
		val output = ByteArrayOutputStream()
		AESCipher.encrypt(ByteArrayInputStream("payload".toByteArray()), output, key)

		val wrongKey: SecretKey = AESCipher.generateSecretKey()
		assertThatThrownBy {
			AESCipher.decrypt(ByteArrayInputStream(output.toByteArray()), wrongKey)
				.bufferedReader().use(BufferedReader::readText)
		}.isInstanceOf(Exception::class.java)
	}

	@Test
	@Throws(NoSuchAlgorithmException::class)
	fun generateSecretKey() {
		val key: SecretKey = AESCipher.generateSecretKey()
		val encodedKey = Base64.getEncoder().encodeToString(key.encoded)
		val keyBuilt: SecretKey = AESCipher.secretKeyFromBase64(encodedKey)
		Assertions.assertThat(key)
			.usingRecursiveComparison(
				RecursiveComparisonConfiguration.builder().withIgnoredFields("key").build()
			)
			.isEqualTo(keyBuilt)
		Assertions.assertThat(key.algorithm).isEqualTo(keyBuilt.algorithm)
		Assertions.assertThat(key.encoded).isEqualTo(keyBuilt.encoded)
		Assertions.assertThat(key.format).isEqualTo(keyBuilt.format)
	}

	@Test
	fun generateSecretKeyProducesDistinctKeys() {
		val first = AESCipher.generateSecretKey()
		val second = AESCipher.generateSecretKey()
		Assertions.assertThat(first.encoded).isNotEqualTo(second.encoded)
	}
}
