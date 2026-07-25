package io.komune.c2.chaincode.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EndorserTest {

    @Test
    fun `fromStringPair parses peer colon organisation`() {
        assertThat(Endorser.fromStringPair("peer0:bclan"))
            .isEqualTo(Endorser(peer = "peer0", organisation = "bclan"))
    }

    @Test
    fun `fromStringPair rejects a value without a colon`() {
        assertThatThrownBy { Endorser.fromStringPair("peer0-bclan") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Bad endorser argument[peer0-bclan]")
    }

    @Test
    fun `fromStringPair rejects a value with too many colons`() {
        assertThatThrownBy { Endorser.fromStringPair("peer0:bclan:extra") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `fromListPair parses a comma separated list preserving order`() {
        assertThat(Endorser.fromListPair("peer0:org1,peer1:org2")).containsExactly(
            Endorser("peer0", "org1"),
            Endorser("peer1", "org2"),
        )
    }

    @Test
    fun `fromListPair parses a single pair`() {
        assertThat(Endorser.fromListPair("peer0:org1")).containsExactly(Endorser("peer0", "org1"))
    }

    @Test
    fun `fromListPair fails on any malformed element`() {
        assertThatThrownBy { Endorser.fromListPair("peer0:org1,broken") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("broken")
    }
}
