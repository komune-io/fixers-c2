package ssm.sdk.sign.crypto

import java.util.Base64
import javax.crypto.Cipher
import org.assertj.core.api.Assertions
import org.bouncycastle.crypto.CryptoException
import org.junit.jupiter.api.Test

internal class RSACipherTest {

	companion object {
		private const val KEY_NAME = "command/vivi"

		/**
		 * Ciphertext of "legacy message" produced with the previous raw RSA/PKCS#1 v1.5 scheme
		 * for the `command/vivi` key. Stands in for the ciphertexts already persisted on-chain,
		 * which cannot be re-encrypted.
		 */
		private const val LEGACY_CIPHERTEXT =
			"RMe0ik7UdZXK0uRaH6WElPno91+g0BJ0QMU6Wy2JnQmYtjAc5eh/BIp1hj3ydLdp/tj9faCZujusrkld0T5J0Hxcgu" +
				"vJa5AwV4IXvPMsmxD0cGfRB66f2QYiE9+dfx2XSCZjwcCFxvO//MqzJNb1ksTKaUqDpA/Vu8Cj/ypXw5O7ImBY" +
				"lXYbzAg7CEXHlJkysl/ObYOP65af3yWEnqSy0wqDWhrglmGLh5wM7dS5Hx+oAHVajhs+oBl8sZVcSr3RGH2qEs" +
				"bqfFUff6ji8S+cV8CqwI01z1e1Zelwcb8u49WuDAXc71gE/lJAH1HnXQErMKVSVtvzOpKKtMO05ZhIMA=="
		private const val LEGACY_MESSAGE = "legacy message"
	}

	private val keyPair = KeyPairReader.loadKeyPair(KEY_NAME)

	@Test
	fun encryptShouldProduceVersionedCiphertext() {
		val encrypted = RSACipher.encrypt("msg to encrypt".toByteArray(), keyPair.public)
		Assertions.assertThat(encrypted).startsWith(RSACipher.VERSION_PREFIX)
	}

	@Test
	fun encryptShouldNotBeDeterministic() {
		val message = "msg to encrypt".toByteArray()
		Assertions.assertThat(RSACipher.encrypt(message, keyPair.public))
			.isNotEqualTo(RSACipher.encrypt(message, keyPair.public))
	}

	@Test
	fun roundTrip() {
		val encrypted = RSACipher.encrypt("msg to encrypt and decrypt".toByteArray(), keyPair.public)
		val value = RSACipher.decrypt(encrypted, keyPair.private)
		Assertions.assertThat(value).isEqualTo("msg to encrypt and decrypt")
	}

	@Test
	fun roundTripShouldSupportEmptyPayload() {
		val encrypted = RSACipher.encrypt(ByteArray(0), keyPair.public)
		Assertions.assertThat(RSACipher.decrypt(encrypted, keyPair.private)).isEmpty()
	}

	@Test
	fun roundTripShouldSupportPayloadLargerThanTheModulus() {
		// The previous scheme was capped at keysize - 11 bytes (245 for a 2048-bit key).
		val message = "large payload ".repeat(10_000)
		val encrypted = RSACipher.encrypt(message.toByteArray(), keyPair.public)
		Assertions.assertThat(RSACipher.decrypt(encrypted, keyPair.private)).isEqualTo(message)
	}

	@Test
	fun decryptShouldReadLegacyPkcs1Ciphertexts() {
		Assertions.assertThat(RSACipher.decrypt(LEGACY_CIPHERTEXT, keyPair.private))
			.isEqualTo(LEGACY_MESSAGE)
	}

	@Test
	fun decryptShouldReadLegacyCiphertextsGeneratedAtRuntime() {
		val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
		cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
		val legacy = Base64.getEncoder().encodeToString(cipher.doFinal("older message".toByteArray()))
		Assertions.assertThat(RSACipher.decrypt(legacy, keyPair.private)).isEqualTo("older message")
	}

	@Test
	fun decryptShouldRejectTamperedCiphertext() {
		val encrypted = RSACipher.encrypt("msg to tamper with".toByteArray(), keyPair.public)
		val envelope = Base64.getDecoder().decode(encrypted.removePrefix(RSACipher.VERSION_PREFIX))
		envelope[envelope.size - 1] = (envelope[envelope.size - 1].toInt() xor 0x01).toByte()
		val tampered = RSACipher.VERSION_PREFIX + Base64.getEncoder().encodeToString(envelope)

		Assertions.assertThatThrownBy { RSACipher.decrypt(tampered, keyPair.private) }
			.isInstanceOf(CryptoException::class.java)
	}

	@Test
	fun decryptShouldRejectTamperedWrappedKey() {
		val encrypted = RSACipher.encrypt("msg to tamper with".toByteArray(), keyPair.public)
		val envelope = Base64.getDecoder().decode(encrypted.removePrefix(RSACipher.VERSION_PREFIX))
		envelope[envelope.size / 2] = (envelope[envelope.size / 2].toInt() xor 0x01).toByte()
		val tampered = RSACipher.VERSION_PREFIX + Base64.getEncoder().encodeToString(envelope)

		Assertions.assertThatThrownBy { RSACipher.decrypt(tampered, keyPair.private) }
			.isInstanceOf(CryptoException::class.java)
	}

	@Test
	fun decryptShouldFailWithAnotherKey() {
		val encrypted = RSACipher.encrypt("msg to encrypt".toByteArray(), keyPair.public)
		val otherKeyPair = KeyPairReader.generateRSAKey()

		Assertions.assertThatThrownBy { RSACipher.decrypt(encrypted, otherKeyPair.private) }
			.isInstanceOf(CryptoException::class.java)
	}

	@Test
	fun decryptShouldFailOnMalformedCiphertext() {
		Assertions.assertThatThrownBy { RSACipher.decrypt("${RSACipher.VERSION_PREFIX}not-base64!", keyPair.private) }
			.isInstanceOf(CryptoException::class.java)
	}
}
