package io.komune.c2.chaincode.api.gateway.blockchain

import io.komune.c2.chaincode.api.fabric.FabricGatewayBlockClient
import io.komune.c2.chaincode.dsl.Block
import io.komune.c2.chaincode.dsl.IdentitiesInfo
import io.komune.c2.chaincode.dsl.InvokeFunction
import io.komune.c2.chaincode.dsl.Transaction
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/** A gateway query is routed by its function and first value to the block or transaction read it names. */
class BlockchainServiceTest {

    private val client = mockk<FabricGatewayBlockClient> {
        every { queryAllBlocksIds(channelId = CHANNEL) } returns listOf(0L, 1L)
        every { queryBlockByNumber(channelId = CHANNEL, blockId = 0L) } returns
            block(blockId = 0, transactionIds = listOf("tx-a"))
        every { queryBlockByNumber(channelId = CHANNEL, blockId = 1L) } returns
            block(blockId = 1, transactionIds = listOf("tx-b", "tx-c"))
        every { queryTransactionById(channelId = CHANNEL, transactionId = "tx-b") } returns
            transaction(transactionId = "tx-b", blockId = 1)
    }
    private val service = BlockchainService(client)

    @Test
    fun `a block list query answers every block id`() {
        assertThat(service.query(CHANNEL, args(function = "list", "block"))).isEqualTo("[0,1]")
    }

    @Test
    fun `a block query answers the block of that number`() {
        assertThat(service.query(CHANNEL, args(function = "block", "1")))
            .contains(""""blockId":1""")
            .contains(""""tx-b"""")
            .contains(""""tx-c"""")
    }

    @Test
    fun `a transaction list query answers the transactions of every block, in block order`() {
        val answer = service.query(CHANNEL, args(function = "list", "transaction"))

        assertThat(Regex(""""transactionId":"(tx-[a-c])"""").findAll(answer).map { it.groupValues[1] }.toList())
            .containsExactly("tx-a", "tx-b", "tx-c")
    }

    @Test
    fun `a transaction query answers the transaction of that id`() {
        assertThat(service.query(CHANNEL, args(function = "transaction", "tx-b")))
            .contains(""""transactionId":"tx-b"""")
            .contains(""""blockId":1""")
    }

    @Test
    fun `a query that names neither a block nor a transaction is refused`() {
        assertThatThrownBy { val _ = service.query(CHANNEL, args(function = "query", "session")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("must be block or transaction")
    }

    private companion object {
        const val CHANNEL = "sandbox"

        fun args(function: String, vararg values: String) =
            InvokeArgs(function = InvokeFunction(function), values = values.toList())

        fun transaction(transactionId: String, blockId: Int) = Transaction(
            transactionId = transactionId,
            blockId = blockId,
            timestamp = 0L,
            isValid = true,
            channelId = CHANNEL,
            creator = IdentitiesInfo(id = "admin", mspid = "BlockchainLANCoopMSP"),
            nonce = ByteArray(0),
        )

        fun block(blockId: Int, transactionIds: List<String>) = Block(
            blockId = blockId,
            previousHash = ByteArray(0),
            dataHash = ByteArray(0),
            transactions = transactionIds.map { transaction(transactionId = it, blockId = blockId) },
        )
    }
}
