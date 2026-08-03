@file:Suppress("TooGenericExceptionCaught", "SwallowedException")

package ssm.sdk.sign.crypto

import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.crypto.CryptoException

object AESCipher {
	private const val ALGO = "AES"
	private const val TRANSFORMATION = "AES/GCM/NoPadding"
	private const val GCM_IV_LENGTH_BYTES = 12
	private const val GCM_TAG_LENGTH_BITS = 128

	@Throws(NoSuchAlgorithmException::class)
	fun generateSecretKey(): SecretKey {
		val kg = KeyGenerator.getInstance(ALGO)
		kg.init(SecureRandom())
		return kg.generateKey()
	}

	fun secretKeyFromBase64(b64Key: String?): SecretKey {
		val key = Base64.getDecoder().decode(b64Key)
		return SecretKeySpec(key, ALGO)
	}

	@Throws(CryptoException::class)
	fun decrypt(fileInput: InputStream, key: SecretKey): InputStream {
		try {
			return getDecryptCipher(key, fileInput)
		} catch (e: Exception) {
			throw CryptoException("Error decrypting file", e)
		}
	}

	@Throws(CryptoException::class)
	fun encrypt(file: File, outputStream: OutputStream?, key: SecretKey?) {
		var fileInput: FileInputStream? = null
		try {
			fileInput = FileInputStream(file)
			encrypt(fileInput, outputStream, key)
		} catch (e: Exception) {
			throw CryptoException("Error encrypting file:" + file.name, e)
		} finally {
			try {
				fileInput?.close()
			} catch (e: IOException) {
				// Silent
			}
		}
	}

	@Throws(CryptoException::class)
	fun encrypt(fileInput: InputStream?, outputStream: OutputStream?, key: SecretKey?) {
		var output: OutputStream? = null
		try {
			output = getEncryptCipher(outputStream, key)
			fileInput!!.copyTo(output)
		} catch (e: Exception) {
			throw CryptoException("Error encrypting:", e)
		} finally {
			try {
				output?.close()
			} catch (e: IOException) {
				// Silent
			}
		}
	}

	@Throws(CryptoException::class)
	private fun getDecryptCipher(key: SecretKey, fileInput: InputStream): CipherInputStream {
		try {
			val iv = ByteArray(GCM_IV_LENGTH_BYTES)
			DataInputStream(fileInput).readFully(iv)
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
			return CipherInputStream(fileInput, cipher)
		} catch (e: Exception) {
			throw CryptoException("Error decrypting", e)
		}
	}

	@Throws(CryptoException::class)
	private fun getEncryptCipher(fileOutput: OutputStream?, key: SecretKey?): CipherOutputStream {
		try {
			val iv = ByteArray(GCM_IV_LENGTH_BYTES).also(SecureRandom()::nextBytes)
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
			fileOutput!!.write(iv)
			return CipherOutputStream(fileOutput, cipher)
		} catch (e: Exception) {
			throw CryptoException("Error encrypting", e)
		}
	}
}
