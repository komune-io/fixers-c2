@file:Suppress("TooGenericExceptionCaught", "SwallowedException")

package ssm.sdk.sign.crypto

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.crypto.CryptoException

/**
 * AES-GCM helper for the local key material handled by the SDK.
 *
 * Each encryption draws a fresh random IV, which is written as the first [IV_LENGTH] bytes of the
 * output and read back on decryption. GCM also authenticates the ciphertext, so tampering fails
 * loudly instead of yielding garbage plaintext.
 */
object AESCipher {
	private const val ALGO = "AES"
	private const val TRANSFORMATION = "AES/GCM/NoPadding"
	private const val KEY_LENGTH_BITS = 256
	private const val IV_LENGTH = 12
	private const val TAG_LENGTH_BITS = 128

	private val random = SecureRandom()

	@Throws(NoSuchAlgorithmException::class)
	fun generateSecretKey(): SecretKey {
		val kg = KeyGenerator.getInstance(ALGO)
		kg.init(KEY_LENGTH_BITS, SecureRandom())
		return kg.generateKey()
	}

	fun secretKeyFromBase64(b64Key: String?): SecretKey {
		val key = Base64.getDecoder().decode(b64Key)
		return SecretKeySpec(key, ALGO)
	}

	@Throws(CryptoException::class)
	fun decrypt(fileInput: InputStream, key: SecretKey): InputStream {
		try {
			val payload = fileInput.readBytes()
			val iv = payload.copyOfRange(0, IV_LENGTH)
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
			return ByteArrayInputStream(cipher.doFinal(payload, IV_LENGTH, payload.size - IV_LENGTH))
		} catch (e: Exception) {
			throw CryptoException("Error decrypting", e)
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
		try {
			val iv = ByteArray(IV_LENGTH).also(random::nextBytes)
			val cipher = Cipher.getInstance(TRANSFORMATION)
			cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
			outputStream!!.write(iv)
			outputStream.write(cipher.doFinal(fileInput!!.readBytes()))
			outputStream.flush()
		} catch (e: Exception) {
			throw CryptoException("Error encrypting:", e)
		} finally {
			try {
				outputStream?.close()
			} catch (e: IOException) {
				// Silent
			}
		}
	}
}
