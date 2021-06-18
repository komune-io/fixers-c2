package ssm.sdk.sign.crypto

import org.bouncycastle.crypto.CryptoException
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import javax.crypto.Cipher

class RSACipher {
    companion object {
        private const val ALGO = "RSA"

        @Throws(CryptoException::class)
        fun encrypt(value: ByteArray?, publicKey: PublicKey?): String {
            try {
                val cipher = Cipher.getInstance(ALGO)
                cipher.init(Cipher.ENCRYPT_MODE, publicKey)
                val enc = cipher.doFinal(value)
                return Base64.getEncoder().encodeToString(enc)
            } catch (e: Exception) {
                throw CryptoException("Error encrypting:", e)
            }
        }

        @Throws(CryptoException::class)
        fun decrypt(value: String?, privateKey: PrivateKey?): String {
            try {
                val bytVal = Base64.getDecoder().decode(value)
                val cipher = Cipher.getInstance(ALGO)
                cipher.init(Cipher.DECRYPT_MODE, privateKey)
                val dec = cipher.doFinal(bytVal)
                return String(dec)
            } catch (e: Exception) {
                throw CryptoException("Error decrypting:", e)
            }
        }
    }
}