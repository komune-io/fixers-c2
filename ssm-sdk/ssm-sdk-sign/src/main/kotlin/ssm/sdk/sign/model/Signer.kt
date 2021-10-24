package ssm.sdk.sign.model

import java.security.KeyPair
import java.util.Objects
import ssm.sdk.sign.crypto.KeyPairReader


typealias SignerName = String

open class Signer(val name: SignerName, val pair: KeyPair) {

	companion object {
		@Throws(Exception::class)
		fun loadFromFile(filename: String): Signer {
			val keypair = KeyPairReader.loadKeyPair(filename)
			return Signer(filename, keypair)
		}

		@Throws(Exception::class)
		fun loadFromFile(name: SignerName, filename: String?): Signer {
			val keypair = KeyPairReader.loadKeyPair(filename!!)
			return Signer(name, keypair)
		}

		@Throws(Exception::class)
		fun generate(name: SignerName): Signer {
			val keypair = KeyPairReader.generateRSAKey()
			return Signer(name, keypair)
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this.javaClass != other.javaClass) return false
		val signer = other as Signer
		return this.name == signer.name && this.pair == signer.pair
	}

	override fun hashCode(): Int {
		return Objects.hash(name, pair)
	}
}
