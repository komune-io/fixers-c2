package ssm.sdk.sign.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SignerTest {

    companion object {
        const val KEY_NAME = "vivi"
        const val KEY_FILE = "command/vivi"
    }

    @Test
    fun `signer admin loads its key pair from a file`() {
        val signer = SignerAdmin.loadFromFile(KEY_FILE)
        assertThat(signer.name).isEqualTo(KEY_FILE)
        assertThat(signer.pair.private).isNotNull
        assertThat(signer.pair.public).isNotNull
    }

    @Test
    fun `signer admin loads with an explicit name and falls back to it as filename`() {
        val named = SignerAdmin.loadFromFile(KEY_NAME, KEY_FILE)
        assertThat(named.name).isEqualTo(KEY_NAME)
        assertThat(named.pair.public).isNotNull

        val fallback = SignerAdmin.loadFromFile(KEY_FILE, null)
        assertThat(fallback.name).isEqualTo(KEY_FILE)
        assertThat(fallback.pair.public).isNotNull
    }

    @Test
    fun `signer user loads its key pair from a file`() {
        val signer = SignerUser.loadFromFile(KEY_FILE)
        assertThat(signer.name).isEqualTo(KEY_FILE)
        assertThat(signer.pair.private).isNotNull
        assertThat(signer.pair.public).isNotNull
    }

    @Test
    fun `signer user loads with an explicit name`() {
        val signer = SignerUser.loadFromFile(KEY_NAME, KEY_FILE)
        assertThat(signer.name).isEqualTo(KEY_NAME)
        assertThat(signer.pair.public).isNotNull
    }

    @Test
    fun `signer user generates a fresh rsa key pair`() {
        val signer = SignerUser.generate("generated")
        assertThat(signer.name).isEqualTo("generated")
        assertThat(signer.pair.private.algorithm).isEqualTo("RSA")
        assertThat(signer.pair.public.algorithm).isEqualTo("RSA")
    }
}
