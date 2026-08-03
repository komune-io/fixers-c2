package io.komune.c2.chaincode.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BlockTest {

    private fun transaction(id: TransactionId = "tx-1") = Transaction(
        transactionId = id,
        blockId = 10,
        timestamp = 1738410430231L,
        isValid = true,
        channelId = "sandbox",
        creator = IdentitiesInfo(id = "identity-1", mspid = "PeerKomune"),
        nonce = byteArrayOf(1, 2, 3),
    )

    @Test
    fun `a block exposes its id hashes and transactions`() {
        val block = Block(
            blockId = 10,
            previousHash = byteArrayOf(1),
            dataHash = byteArrayOf(2),
            transactions = listOf(transaction()),
        )
        assertThat(block.blockId).isEqualTo(10)
        assertThat(block.previousHash).containsExactly(1)
        assertThat(block.dataHash).containsExactly(2)
        assertThat(block.transactions).hasSize(1)
    }

    @Test
    fun `a transaction exposes its fields`() {
        val transaction = transaction()
        assertThat(transaction.transactionId).isEqualTo("tx-1")
        assertThat(transaction.blockId).isEqualTo(10)
        assertThat(transaction.timestamp).isEqualTo(1738410430231L)
        assertThat(transaction.isValid).isTrue
        assertThat(transaction.channelId).isEqualTo("sandbox")
        assertThat(transaction.creator.id).isEqualTo("identity-1")
        assertThat(transaction.creator.mspid).isEqualTo("PeerKomune")
        assertThat(transaction.nonce).containsExactly(1, 2, 3)
    }
}
