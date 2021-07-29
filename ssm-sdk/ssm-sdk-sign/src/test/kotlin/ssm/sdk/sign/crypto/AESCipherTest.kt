package ssm.sdk.sign.crypto

import org.assertj.core.api.Assertions
import org.bouncycastle.crypto.CryptoException
import org.junit.jupiter.api.Test
import ssm.sdk.sign.utils.FileUtils
import java.io.*
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.SecretKey


internal class AESCipherTest {

    companion object {
        const val FILE_TO_COMMIT_TXT = "crypto/fileToCommit.txt"
        const val FILE_TO_COMMIT_ENCRYPTED = "crypto/fileToCommit.encrypted"
    }

    @Test
    @Throws(IOException::class, CryptoException::class)
    fun encrypt() {
        val fileToEncrypt: File = FileUtils.getFile(FILE_TO_COMMIT_TXT)
        val encryptedFile = File.createTempFile("enc_", "tmp")
        val encryptedFileProof: File = FileUtils.getFile(FILE_TO_COMMIT_ENCRYPTED)
        try {
            val os = FileOutputStream(encryptedFile)
            val key: SecretKey = AESCipher.secretKeyFromBase64("+cRaRuaSK1/RObE9oEOm6Q==")
            AESCipher.encrypt(fileToEncrypt, os, key)
            val te = FileUtils.sameContent(encryptedFile.toPath(), encryptedFileProof.toPath())
            Assertions.assertThat(te).isTrue
        } finally {
            encryptedFile.delete()
        }
    }

    @Test
    @Throws(IOException::class, CryptoException::class)
    fun decrypt() {
        val encryptedFile: File = FileUtils.getFile(FILE_TO_COMMIT_ENCRYPTED)
        try {
            val key: SecretKey = AESCipher.secretKeyFromBase64("+cRaRuaSK1/RObE9oEOm6Q==")
            val decryptedStream: InputStream = AESCipher.decrypt(FileInputStream(encryptedFile), key)
            val value = decryptedStream.bufferedReader().use(BufferedReader::readText)
            Assertions.assertThat(value).isEqualTo("to commit")
        } finally {
            encryptedFile.delete()
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun generateSecretKey() {
        val key: SecretKey = AESCipher.generateSecretKey()
        val encodedKey = Base64.getEncoder().encodeToString(key.encoded)
        val keyBuilded: SecretKey = AESCipher.secretKeyFromBase64(encodedKey)
        Assertions.assertThat(key).isEqualToComparingFieldByField(keyBuilded)
    }
}