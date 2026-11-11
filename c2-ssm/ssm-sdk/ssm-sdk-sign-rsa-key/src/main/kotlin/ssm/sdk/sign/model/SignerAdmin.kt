package ssm.sdk.sign.model

import java.security.KeyPair
import ssm.chaincode.dsl.model.AgentName
import ssm.sdk.sign.crypto.KeyPairReader

class SignerAdmin(
    override val name: AgentName,
    override val pair: KeyPair
) : Signer {

    companion object {
        @Throws(Exception::class)
        fun loadFromFile(filename: String): SignerAdmin {
            val keypair = KeyPairReader.loadKeyPair(filename)
            return SignerAdmin(filename, keypair)
        }

        @Throws(Exception::class)
        fun loadFromFile(name: String, filename: String?): SignerAdmin {
            val keypair = KeyPairReader.loadKeyPair(filename ?: name)
            return SignerAdmin(name, keypair)
        }
    }
}
