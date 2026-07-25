package io.komune.c2.chaincode.api.config

import io.komune.c2.chaincode.dsl.Endorser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ChannelConfigTest {

    private val user = C2ChaincodeConfiguration.UserConfig().apply {
        name = "Admin"
        password = ""
        org = "bclan"
    }
    private val fileConfig = C2ChaincodeConfiguration.FileConfig().apply {
        file = "config.json"
        crypto = "/crypto"
    }
    private val endorsers = listOf(Endorser("peer0", "bclan"))

    @Test
    fun `ChannelChaincodePair fromConfig parses channel slash chaincode`() {
        assertThat(ChannelChaincodePair.fromConfig("sandbox/ssm"))
            .isEqualTo(ChannelChaincodePair(channelId = "sandbox", chainCodeId = "ssm"))
    }

    @Test
    fun `ChannelChaincodePair fromConfig rejects malformed values`() {
        assertThatThrownBy { ChannelChaincodePair.fromConfig("sandbox-ssm") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Bad ccid argument[sandbox-ssm]")
        assertThatThrownBy { ChannelChaincodePair.fromConfig("a/b/c") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `ChannelConfig fromConfig groups chaincodes by channel`() {
        val configs = ChannelConfig.fromConfig(
            lines = arrayOf("ch1/cc1", "ch1/cc2", "ch2/cc1"),
            user = user,
            config = fileConfig,
            endorsers = endorsers,
        )

        assertThat(configs.keys).containsExactlyInAnyOrder("ch1", "ch2")
        assertThat(configs.getValue("ch1").chaincodeId).containsExactly("cc1", "cc2")
        assertThat(configs.getValue("ch2").chaincodeId).containsExactly("cc1")
        assertThat(configs.getValue("ch1").endorsers).isEqualTo(endorsers)
        assertThat(configs.getValue("ch1").user).isSameAs(user)
    }
}
