package io.komune.c2.chaincode.api.config

import f2.dsl.fnc.operators.Batch
import io.komune.c2.chaincode.dsl.Endorser
import io.komune.c2.chaincode.dsl.invoke.InvokeException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class C2ChaincodeConfigurationTest {

    private fun configuration(
        defaultCcid: String = "sandbox/ssm",
        ccid: String = "sandbox/ssm,sandbox/ssm2",
        batch: BatchProperties? = null,
    ) = C2ChaincodeConfiguration(
        defaultCcid = defaultCcid,
        ccid = ccid,
        user = C2ChaincodeConfiguration.UserConfig().apply {
            name = "Admin"
            password = ""
            org = "bclan"
        },
        config = null,
        endorsers = "peer0:bclan,peer1:org2",
        batch = batch,
    )

    @Test
    fun `getCcids splits the comma separated list`() {
        assertThat(configuration().getCcids()).containsExactly("sandbox/ssm", "sandbox/ssm2")
    }

    @Test
    fun `getEndorsers parses the endorser pairs`() {
        assertThat(configuration().getEndorsers()).containsExactly(
            Endorser("peer0", "bclan"),
            Endorser("peer1", "org2"),
        )
    }

    @Test
    fun `getChannelChaincodePair falls back to the default ccid when nothing is given`() {
        assertThat(configuration().getChannelChaincodePair(null, null))
            .isEqualTo(ChannelChaincodePair("sandbox", "ssm"))
    }

    @Test
    fun `getChannelChaincodePair accepts an explicit pair declared in ccid`() {
        assertThat(configuration().getChannelChaincodePair("sandbox", "ssm2"))
            .isEqualTo(ChannelChaincodePair("sandbox", "ssm2"))
    }

    @Test
    fun `getChannelChaincodePair completes a partial pair from the default`() {
        assertThat(configuration().getChannelChaincodePair(null, "ssm2"))
            .isEqualTo(ChannelChaincodePair("sandbox", "ssm2"))
    }

    @Test
    fun `getChannelChaincodePair rejects a pair not declared in ccid`() {
        assertThatThrownBy { configuration().getChannelChaincodePair("other", "ssm") }
            .isInstanceOf(InvokeException::class.java)
            .hasMessageContaining("Invalid other/ssm")
    }

    @Test
    fun `getBatch defaults when no batch properties are bound`() {
        assertThat(configuration(batch = null).getBatch()).isEqualTo(Batch())
    }

    @Test
    fun `getBatch maps bound size and concurrency`() {
        val batch = configuration(
            batch = BatchProperties().apply {
                size = 1024
                concurrency = 7
            },
        ).getBatch()
        assertThat(batch).isEqualTo(Batch(size = 1024, concurrency = 7))
    }
}
