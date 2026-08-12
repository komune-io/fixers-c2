@file:Suppress("TooGenericExceptionCaught")

package ssm.sdk.sign.crypto

import java.nio.ByteBuffer
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.crypto.CryptoException

/**
 * Encryption of private messages persisted on the ledger.
 *
 * New ciphertexts use a hybrid scheme: a random AES-256 key encrypts the payload with GCM
 * (authenticated), and that key is wrapped with RSA-OAEP (SHA-256 digest, MGF1-SHA-256).
 * The result is prefixed with [VERSION_PREFIX] so it is distinguishable from the legacy format.
 *
 * Envelope layout (before Base64):
 * ```
 * [2 bytes: wrapped key length][wrapped AES key][12 bytes: GCM IV][AES-GCM ciphertext + 16 bytes tag]
 * ```
 *
 * Ciphertexts written before this scheme existed are raw RSA/PKCS#1 v1.5 and cannot be
 * re-encrypted (they are on-chain forever), so [decrypt] still reads them when the version
 * prefix is absent. Encryption always uses the new scheme.
 */
object RSACipher {
	/** Marker identifying the hybrid RSA-OAEP + AES-GCM format. Not part of the Base64 alphabet. */
	const val VERSION_PREFIX = "v2:"

	private const val RSA_OAEP = "RSA/ECB/OAEPPadding"
	private const val RSA_LEGACY_PKCS1 = "RSA/ECB/PKCS1Padding"
	private const val AES = "AES"
	private const val AES_GCM = "AES/GCM/NoPadding"
	private const val AES_KEY_SIZE_BITS = 256
	private const val GCM_IV_SIZE = 12
	private const val GCM_TAG_SIZE_BITS = 128
	private const val WRAPPED_KEY_LENGTH_SIZE = 2

	@Throws(CryptoException::class)
	fun encrypt(value: ByteArray?, publicKey: PublicKey?): String {
		try {
			val secretKey = generateSecretKey()
			val iv = ByteArray(GCM_IV_SIZE).also { SecureRandom().nextBytes(it) }
			val cipherText = aesGcm(Cipher.ENCRYPT_MODE, secretKey, iv).doFinal(value)
			val wrappedKey = wrapKey(secretKey, publicKey)
			val envelope = ByteBuffer.allocate(
				WRAPPED_KEY_LENGTH_SIZE + wrappedKey.size + GCM_IV_SIZE + cipherText.size
			)
				.putShort(wrappedKey.size.toShort())
				.put(wrappedKey)
				.put(iv)
				.put(cipherText)
				.array()
			return VERSION_PREFIX + Base64.getEncoder().encodeToString(envelope)
		} catch (e: Exception) {
			throw CryptoException("Error encrypting:", e)
		}
	}

	@Throws(CryptoException::class)
	fun decrypt(value: String?, privateKey: PrivateKey?): String {
		try {
			val decrypted = if (value != null && value.startsWith(VERSION_PREFIX)) {
				decryptEnvelope(value.substring(VERSION_PREFIX.length), privateKey)
			} else {
				decryptLegacy(value, privateKey)
			}
			return String(decrypted)
		} catch (e: Exception) {
			throw CryptoException("Error decrypting:", e)
		}
	}

	private fun decryptEnvelope(base64Envelope: String, privateKey: PrivateKey?): ByteArray {
		val buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64Envelope))
		val wrappedKey = ByteArray(buffer.short.toInt())
		buffer.get(wrappedKey)
		val iv = ByteArray(GCM_IV_SIZE)
		buffer.get(iv)
		val cipherText = ByteArray(buffer.remaining())
		buffer.get(cipherText)
		return aesGcm(Cipher.DECRYPT_MODE, unwrapKey(wrappedKey, privateKey), iv).doFinal(cipherText)
	}

	/** Reads ciphertexts produced by the previous raw RSA/PKCS#1 v1.5 scheme. Never used to encrypt. */
	private fun decryptLegacy(value: String?, privateKey: PrivateKey?): ByteArray {
		val cipher = Cipher.getInstance(RSA_LEGACY_PKCS1)
		cipher.init(Cipher.DECRYPT_MODE, privateKey)
		return cipher.doFinal(Base64.getDecoder().decode(value))
	}

	private fun generateSecretKey(): SecretKey = KeyGenerator.getInstance(AES)
		.apply { init(AES_KEY_SIZE_BITS, SecureRandom()) }
		.generateKey()

	private fun wrapKey(secretKey: SecretKey, publicKey: PublicKey?): ByteArray {
		val cipher = Cipher.getInstance(RSA_OAEP)
		cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameters())
		return cipher.doFinal(secretKey.encoded)
	}

	private fun unwrapKey(wrappedKey: ByteArray, privateKey: PrivateKey?): SecretKey {
		val cipher = Cipher.getInstance(RSA_OAEP)
		cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParameters())
		return SecretKeySpec(cipher.doFinal(wrappedKey), AES)
	}

	private fun aesGcm(mode: Int, secretKey: SecretKey, iv: ByteArray): Cipher {
		return Cipher.getInstance(AES_GCM).apply {
			init(mode, secretKey, GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))
		}
	}

	/** MGF1 is pinned to SHA-256 explicitly: providers default it to SHA-1 for this transformation. */
	private fun oaepParameters() = OAEPParameterSpec(
		"SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
	)
}
