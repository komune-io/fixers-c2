package ssm.sdk.sign.extention

import java.security.PublicKey
import ssm.chaincode.dsl.model.Agent
import ssm.sdk.sign.crypto.KeyPairReader

@Throws(Exception::class)
fun Agent.Companion.loadFromFile(name: String): Agent {
	val pub = KeyPairReader.loadPublicKey(name)
	return Agent(name, pub.encoded)
}

@Throws(Exception::class)
fun Agent.Companion.loadFromFile(name: String, filename: String): Agent {
	val pub = KeyPairReader.loadPublicKey(filename)
	return Agent(name, pub.encoded)
}

@Throws(Exception::class)
fun Agent.getPubAsKey(): PublicKey {
	return KeyPairReader.fromByteArray(pub)
}
