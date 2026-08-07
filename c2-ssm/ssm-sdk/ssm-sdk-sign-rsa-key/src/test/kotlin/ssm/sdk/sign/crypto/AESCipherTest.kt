package ssm.sdk.sign.crypto

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.NoSuchAlgorithmException
import java.util.Base64
import javax.crypto.SecretKey
import org.assertj.core.api.Assertions
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.bouncycastle.crypto.CryptoException
import org.junit.jupiter.api.Test
import ssm.sdk.sign.FileUtils


internal class AESCipherTest {

	companion object {
		const val FILE_TO_COMMIT_TXT = "crypto/fileToCommit.txt"
		const val FILE_TO_COMMIT_ENCRYPTED = "crypto/fileToCommit.encrypted"
	}

	@Test
	@Throws(IOException::class, CryptoException::class)
	fun encrypt() {
		val fileToEncrypt: File = FileUtils.getFile(FILE_TO_COMMIT_TXT)
		val encryptedFile = File.createTempFile("enc_", "tmp")
		val encryptedFileProof: File = FileUtils.getFile(FILE_TO_COMMIT_ENCRYPTED)
		try {
			val os = FileOutputStream(encryptedFile)
			val key: SecretKey = AESCipher.secretKeyFromBase64("+cRaRuaSK1/RObE9oEOm6Q==")
			AESCipher.encrypt(fileToEncrypt, os, key)
			val te = FileUtils.sameContent(encryptedFile.toPath(), encryptedFileProof.toPath())
			Assertions.assertThat(te).isTrue
		} finally {
			encryptedFile.delete()
		}
	}

	@Test
	@Throws(IOException::class, CryptoException::class)
	fun decrypt() {
		val encryptedFile: File = FileUtils.getFile(FILE_TO_COMMIT_ENCRYPTED)
		val key: SecretKey = AESCipher.secretKeyFromBase64("+cRaRuaSK1/RObE9oEOm6Q==")
		val decryptedStream: InputStream = AESCipher.decrypt(FileInputStream(encryptedFile), key)
		val value = decryptedStream.bufferedReader().use(BufferedReader::readText)
		Assertions.assertThat(value).isEqualTo("to commit")
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
}
