package ssm.sdk.sign.crypto

import java.security.PrivateKey
import java.security.Signature
import java.util.Base64

object Sha256RSASigner {
    @Throws(Exception::class)
    fun rsaSign(plainText: String, key: PrivateKey?): ByteArray {
        val privateSignature = Signature.getInstance("SHA256withRSA")
        privateSignature.initSign(key)
        privateSignature.update(plainText.toByteArray(charset("UTF-8")))
        return privateSignature.sign()
    }

    @Throws(Exception::class)
    fun rsaSignAsB64(plainText: String, key: PrivateKey?): String {
        val value = rsaSign(plainText, key)
        return b64Encode(value)
    }

    private fun b64Encode(value: ByteArray): String = Base64.getEncoder().encodeToString(value)
}
