package ssm.sdk.sign.model

import java.security.KeyPair
import ssm.chaincode.dsl.model.AgentName
import ssm.sdk.sign.crypto.KeyPairReader

data class SignerUser(
    override val name: AgentName,
    override val pair: KeyPair
) : Signer {

    companion object {
        @Throws(Exception::class)
        fun loadFromFile(filename: String): SignerUser {
            val keypair = KeyPairReader.loadKeyPair(filename)
            return SignerUser(filename, keypair)
        }

        @Throws(Exception::class)
        fun loadFromFile(name: AgentName, filename: String?): SignerUser {
            val keypair = KeyPairReader.loadKeyPair(filename!!)
            return SignerUser(name, keypair)
        }

        @Throws(Exception::class)
        fun generate(name: AgentName): SignerUser {
            val keypair = KeyPairReader.generateRSAKey()
            return SignerUser(name, keypair)
        }
    }
}
