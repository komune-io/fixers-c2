package io.komune.c2.ssm.fabric.storing

import io.komune.c2.chaincode.api.fabric.FabricGatewayClient
import io.komune.c2.chaincode.api.fabric.TxOutcome
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import io.komune.c2.chaincode.dsl.invoke.InvokeRequest
import io.komune.c2.chaincode.dsl.invoke.InvokeRequestType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FabricSsmClientTest {

    @Test
    fun `query delegates to FabricGatewayClient and returns first result`() = runTest {
        val fabric = mockk<FabricGatewayClient>()
        val capturedArgs = slot<List<InvokeArgs>>()
        coEvery {
            fabric.query("sandbox", "ssm", capture(capturedArgs))
        } returns listOf("""{"sessionName":"42"}""")

        val repo = FabricSsmClient(fabric)
        val result = repo.query(
            cmd = "query",
            fcn = "GetSessionLogs",
            args = listOf("deliveryAutomate", "42"),
            channelId = "sandbox",
            chaincodeId = "ssm",
        )

        assertEquals("""{"sessionName":"42"}""", result)
        val sent = capturedArgs.captured.single()
        assertEquals("GetSessionLogs", sent.function)
        assertEquals(listOf("deliveryAutomate", "42"), sent.values)
    }

    @Test
    fun `query throws when channelId is null`() = runTest {
        val repo = FabricSsmClient(mockk())
        assertThrows<IllegalArgumentException> {
            repo.query("query", "GetSessionLogs", listOf("a"), channelId = null, chaincodeId = "ssm")
        }
    }

    @Test
    fun `invoke maps Committed TxOutcome to Committed CommandOutcome`() = runTest {
        val fabric = mockk<FabricGatewayClient>()
        coEvery {
            fabric.invoke("sandbox", "ssm", any<List<InvokeArgs>>(), listOf("msg-1"))
        } returns listOf(
            TxOutcome.Committed(msgId = "msg-1", transactionId = "tx-1", blockNumber = 7, payload = "{}"),
        )

        val repo = FabricSsmClient(fabric)
        val outcomes = repo.invoke(
            invokeArgs = listOf(
                InvokeRequest(channelid = "sandbox", chaincodeid = "ssm",
                    cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf("a", "b")),
            ),
            msgIds = listOf("msg-1"),
        )

        assertEquals(1, outcomes.size)
        assertEquals("Committed", outcomes[0].outcome)
        assertEquals("msg-1", outcomes[0].msgId)
        assertEquals("tx-1", outcomes[0].transactionId)
        assertEquals(7L, outcomes[0].blockNumber)
        assertEquals("{}", outcomes[0].payload)
    }

    @Test
    fun `invoke maps every TxOutcome variant`() = runTest {
        val fabric = mockk<FabricGatewayClient>()
        coEvery {
            fabric.invoke("sandbox", "ssm", any<List<InvokeArgs>>(), any<List<String>>())
        } returns listOf(
            TxOutcome.Committed(msgId = "m-c", transactionId = "tx-c", blockNumber = 1, payload = "{}"),
            TxOutcome.Rejected(msgId = "m-r", errorCode = "ENDORSE_FAILED", errorMessage = "bad"),
            TxOutcome.Transient(msgId = "m-t", errorCode = "GRPC_UNAVAILABLE", errorMessage = "down"),
            TxOutcome.Indeterminate(msgId = "m-i", errorCode = "SUBMIT_FAILED", errorMessage = "?"),
            TxOutcome.Conflict(msgId = "m-co", errorCode = "MVCC", errorMessage = "version"),
        )

        val req = InvokeRequest(channelid = "sandbox", chaincodeid = "ssm",
            cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf())
        val repo = FabricSsmClient(fabric)
        val outcomes = repo.invoke(
            invokeArgs = List(5) { req },
            msgIds = listOf("m-c", "m-r", "m-t", "m-i", "m-co"),
        )

        assertEquals(
            listOf("Committed", "Rejected", "Transient", "Indeterminate", "Conflict"),
            outcomes.map { it.outcome },
        )
        val rejected = outcomes.first { it.outcome == "Rejected" }
        assertEquals("ENDORSE_FAILED", rejected.errorCode)
        assertEquals("bad", rejected.errorMessage)
    }

    @Test
    fun `invoke groups commands by (channelId, chaincodeId) before calling FabricGatewayClient`() = runTest {
        val fabric = mockk<FabricGatewayClient>()
        coEvery { fabric.invoke("chan-A", "ssm", any(), any()) } answers {
            arg<List<String>>(3).map { TxOutcome.Committed(it, "tx-$it", 1, "{}") }
        }
        coEvery { fabric.invoke("chan-B", "ssm", any(), any()) } answers {
            arg<List<String>>(3).map { TxOutcome.Committed(it, "tx-$it", 2, "{}") }
        }

        val a = InvokeRequest(channelid = "chan-A", chaincodeid = "ssm",
            cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf())
        val b = InvokeRequest(channelid = "chan-B", chaincodeid = "ssm",
            cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf())

        val repo = FabricSsmClient(fabric)
        val outcomes = repo.invoke(listOf(a, b, a), listOf("a1", "b1", "a2"))

        assertEquals(3, outcomes.size)
        assertTrue(outcomes.all { it.outcome == "Committed" })
        coVerify(exactly = 1) { fabric.invoke("chan-A", "ssm", any(), listOf("a1", "a2")) }
        coVerify(exactly = 1) { fabric.invoke("chan-B", "ssm", any(), listOf("b1")) }
    }

    @Test
    fun `invoke throws when an InvokeRequest has null channelid`() = runTest {
        val repo = FabricSsmClient(mockk())
        val req = InvokeRequest(channelid = null, chaincodeid = "ssm",
            cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf())
        assertThrows<IllegalArgumentException> {
            repo.invoke(listOf(req), listOf("m"))
        }
    }

    @Test
    fun `invoke require msgIds size matches invokeArgs size`() = runTest {
        val repo = FabricSsmClient(mockk())
        val req = InvokeRequest(channelid = "s", chaincodeid = "ssm",
            cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf())
        assertThrows<IllegalArgumentException> {
            repo.invoke(listOf(req, req), listOf("only-one"))
        }
    }

    @Test
    fun `invoke synthesises Indeterminate for failed group and preserves successful ones`() = runTest {
        val fabric = mockk<FabricGatewayClient>()
        coEvery { fabric.invoke("chan-OK", "ssm", any(), any()) } answers {
            arg<List<String>>(3).map { TxOutcome.Committed(it, "tx-$it", 1, "{}") }
        }
        coEvery { fabric.invoke("chan-BAD", "ssm", any(), any()) } throws RuntimeException("gateway gone")

        val ok = InvokeRequest(channelid = "chan-OK", chaincodeid = "ssm",
            cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf())
        val bad = InvokeRequest(channelid = "chan-BAD", chaincodeid = "ssm",
            cmd = InvokeRequestType.invoke, fcn = "Perform", args = arrayOf())

        val outcomes = FabricSsmClient(fabric).invoke(listOf(ok, bad), listOf("ok-1", "bad-1"))

        assertEquals(2, outcomes.size)
        val okOutcome = outcomes.first { it.msgId == "ok-1" }
        val badOutcome = outcomes.first { it.msgId == "bad-1" }
        assertEquals("Committed", okOutcome.outcome)
        assertEquals("Indeterminate", badOutcome.outcome)
        assertEquals("TRANSPORT_ERROR", badOutcome.errorCode)
        assertEquals("gateway gone", badOutcome.errorMessage)
    }
}
