package ssm.sdk.sign.crypto

import java.io.IOException
import java.io.Reader
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPrivateCrtKeySpec
import java.security.spec.X509EncodedKeySpec
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.crypto.CryptoException
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import ssm.sdk.sign.FileUtils

object KeyPairReader {

    private const val RSA_KEY_SIZE = 2048
    private const val ERROR_MESSAGE = "key can't be loaded"
    private const val ERROR_MESSAGE_PRV = "Private $ERROR_MESSAGE"
    private const val ERROR_MESSAGE_PUB = "Public $ERROR_MESSAGE"
    private const val ERROR_MESSAGE_RSA = "RSA $ERROR_MESSAGE"
    private const val RSA_KEY = "RSA"

    @Throws(CryptoException::class)
    fun loadKeyPair(filename: String): KeyPair {
        val prv = loadPrivateKey(filename)
        val pub = loadPublicKey(filename)
        return KeyPair(pub, prv)
    }

    @Throws(CryptoException::class)
    fun loadKeyPair(pubStr: String, prvStr: String): KeyPair {
        val prv = prvStr.reader().use { loadPrivateKey(it) }
        val pub = pubStr.reader().use { loadPublicKey(it) }
        return KeyPair(pub, prv)
    }

    @Throws(CryptoException::class)
    fun loadPrivateKey(filename: String): PrivateKey {
        return try {
            FileUtils.getReader(filename).use { loadPrivateKey(it) }
        } catch (e: IOException) {
            throw CryptoException(ERROR_MESSAGE_PRV, e)
        }
    }

    @Throws(CryptoException::class)
    fun loadPrivateKey(reader: Reader): PrivateKey {
        try {
            val pem = getPemObject(reader)
            val key = RSAPrivateKey.getInstance(pem.content)
            val kf = KeyFactory.getInstance(RSA_KEY)
            val privSpec = RSAPrivateCrtKeySpec(
                key.modulus,
                key.publicExponent,
                key.privateExponent,
                key.prime1,
                key.prime2,
                key.exponent1,
                key.exponent2,
                key.coefficient
            )
            return kf.generatePrivate(privSpec)
        } catch (e: GeneralSecurityException) {
            throw CryptoException(ERROR_MESSAGE_PRV, e)
        } catch (e: IOException) {
            throw CryptoException(ERROR_MESSAGE_PRV, e)
        }
    }

    @Throws(CryptoException::class)
    fun loadPublicKey(filename: String): PublicKey {
        var fixedFileName = filename
        if (!filename.endsWith(".pub")) {
            fixedFileName = "$filename.pub"
        }

        return try {
            FileUtils.getReader(fixedFileName).use { loadPublicKey(it) }
        } catch (e: IOException) {
            throw CryptoException(ERROR_MESSAGE_PUB, e)
        }
    }

    @Throws(CryptoException::class)
    fun loadPublicKey(reader: Reader): PublicKey {
        try {
            val pem = getPemObject(reader)
            val pubKeyBytes = pem.content
            val pubSpec = X509EncodedKeySpec(pubKeyBytes)
            val kf = KeyFactory.getInstance(RSA_KEY)
            return kf.generatePublic(pubSpec)
        } catch (e: GeneralSecurityException) {
            throw CryptoException(ERROR_MESSAGE_PUB, e)
        } catch (e: IOException) {
            throw CryptoException(ERROR_MESSAGE_PUB, e)
        }
    }

    @Throws(CryptoException::class)
    fun fromByteArray(pub: ByteArray?): PublicKey {
        try {
            return KeyFactory.getInstance(RSA_KEY).generatePublic(X509EncodedKeySpec(pub))
        } catch (e: InvalidKeySpecException) {
            throw CryptoException(ERROR_MESSAGE_PUB, e)
        } catch (e: NoSuchAlgorithmException) {
            throw CryptoException(ERROR_MESSAGE_PUB, e)
        }
    }

    @Throws(CryptoException::class)
    fun generateRSAKey(): KeyPair {
        try {
            val kpg = KeyPairGenerator.getInstance(RSA_KEY)
            kpg.initialize(RSA_KEY_SIZE)
            return kpg.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            throw CryptoException(ERROR_MESSAGE_RSA, e)
        }
    }

    // PemReader wraps the caller's reader: closing it would close a stream the caller owns.
    @Suppress("MissingUseCall")
    @Throws(IOException::class)
    private fun getPemObject(reader: Reader): PemObject = PemReader(reader).readPemObject()
}
