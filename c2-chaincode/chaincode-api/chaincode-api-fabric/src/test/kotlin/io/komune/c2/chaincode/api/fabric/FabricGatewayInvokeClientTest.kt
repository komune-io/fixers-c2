package io.komune.c2.chaincode.api.fabric

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.fabric.client.Contract
import org.hyperledger.fabric.client.Proposal
import org.junit.jupiter.api.Test

/**
 * Unit tests for FabricGatewayClient.invoke(List) — no live Fabric required.
 *
 * A fake FabricGatewayBuilder subclass overrides contracts() to return a stub
 * Contract that always throws StatusRuntimeException from newProposal(), so
 * every item is categorised as TxOutcome.Transient without hitting any real
 * gRPC endpoint. The runCatching wrapper inside invoke ensures no exception
 * escapes and the list size is always preserved.
 */
class FabricGatewayInvokeClientTest {

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /**
     * A Contract stub whose newProposal() always throws StatusRuntimeException.
     * This exercises the catch branch in commitTransaction that returns Transient.
     */
    private val throwingContract: Contract = object : Contract {
        override fun getChaincodeName(): String = "stub"
        override fun getContractName(): Optional<String> = Optional.empty()

        override fun newProposal(transactionName: String): Proposal.Builder {
            throw StatusRuntimeException(Status.UNAVAILABLE.withDescription("stub: no gRPC"))
        }

        override fun submitTransaction(name: String): ByteArray = ByteArray(0)
        override fun submitTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
        override fun submitTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)

        override fun evaluateTransaction(name: String): ByteArray = ByteArray(0)
        override fun evaluateTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
        override fun evaluateTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
    }

    /**
     * FabricGatewayBuilder whose contracts() returns a single stubbed Contract.
     * Uses the protected no-arg constructor so FabricConfigLoader is never instantiated.
     * The createGateways() path is dead code here because contracts() is fully overridden.
     */
    private fun stubBuilder(contract: Contract = throwingContract): FabricGatewayBuilder =
        object : FabricGatewayBuilder() {
            override fun contracts(channelId: ChannelId, chaincodeId: ChaincodeId): List<Contract> =
                listOf(contract)
        }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    suspend fun `invoke returns list with same size as input, no exception escapes`() {
        val client = FabricGatewayClient(stubBuilder(), parallelism = 1)

        val outcomes = client.invoke(
            channelId = "ch",
            chaincodeId = "cc",
            invokeArgsList = listOf(
                InvokeArgs("fn", "a"),
                InvokeArgs("fn", "b"),
                InvokeArgs("fn", "c"),
            ),
        )

        // Size must be preserved — no item can cancel siblings.
        assertThat(outcomes).hasSize(3)
        // Each item must be classified (not throw); with a stub that throws UNAVAILABLE,
        // every item lands in TxOutcome.Transient via the gRPC catch branch.
        outcomes.forEach { outcome ->
            assertThat(outcome).isInstanceOf(TxOutcome.Transient::class.java)
        }
    }

    @Test
    suspend fun `invoke preserves per-item commandId in outcomes`() {
        val client = FabricGatewayClient(stubBuilder(), parallelism = 1)

        val outcomes = client.invoke(
            channelId = "ch",
            chaincodeId = "cc",
            invokeArgsList = listOf(
                InvokeArgs("fn", "a"),
                InvokeArgs("fn", "b"),
            ),
            commandIds = listOf("cmd-1", "cmd-2"),
        )

        assertThat(outcomes).hasSize(2)
        assertThat((outcomes[0] as TxOutcome.Transient).msgId).isEqualTo("cmd-1")
        assertThat((outcomes[1] as TxOutcome.Transient).msgId).isEqualTo("cmd-2")
    }

    @Test
    suspend fun `commandIds size mismatch throws IllegalArgumentException before any Fabric call`() {
        val client = FabricGatewayClient(stubBuilder(), parallelism = 1)

        val thrown = runCatching {
            client.invoke(
                channelId = "ch",
                chaincodeId = "cc",
                invokeArgsList = listOf(InvokeArgs("fn", "a")),
                commandIds = listOf("c1", "c2"),  // wrong size
            )
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(thrown!!.message).contains("commandIds.size=2")
    }

    @Test
    suspend fun `a non-gRPC failure is categorised as Transient UNEXPECTED with the exception message`() {
        val unexpectedContract: Contract = object : Contract {
            override fun getChaincodeName(): String = "stub"
            override fun getContractName(): Optional<String> = Optional.empty()

            override fun newProposal(transactionName: String): Proposal.Builder {
                throw IllegalStateException("boom")
            }

            override fun submitTransaction(name: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
        }
        val client = FabricGatewayClient(stubBuilder(unexpectedContract), parallelism = 1)

        val outcomes = client.invoke(
            channelId = "ch",
            chaincodeId = "cc",
            invokeArgsList = listOf(InvokeArgs("fn", "a")),
            commandIds = listOf("cmd-1"),
        )

        val transient = outcomes.single() as TxOutcome.Transient
        assertThat(transient.msgId).isEqualTo("cmd-1")
        assertThat(transient.errorCode).isEqualTo("UNEXPECTED")
        assertThat(transient.errorMessage).isEqualTo("boom")
    }

    @Test
    suspend fun `a non-gRPC failure without message falls back to the exception class name`() {
        val messagelessContract: Contract = object : Contract {
            override fun getChaincodeName(): String = "stub"
            override fun getContractName(): Optional<String> = Optional.empty()

            override fun newProposal(transactionName: String): Proposal.Builder {
                throw IllegalStateException()
            }

            override fun submitTransaction(name: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
        }
        val client = FabricGatewayClient(stubBuilder(messagelessContract), parallelism = 1)

        val outcomes = client.invoke(
            channelId = "ch",
            chaincodeId = "cc",
            invokeArgsList = listOf(InvokeArgs("fn", "a")),
        )

        val transient = outcomes.single() as TxOutcome.Transient
        assertThat(transient.errorCode).isEqualTo("UNEXPECTED")
        assertThat(transient.errorMessage).isEqualTo("IllegalStateException")
    }

    @Test
    suspend fun `one item failure does not cancel sibling items (supervisorScope)`() {
        val callCount = AtomicInteger(0)
        val countingContract: Contract = object : Contract {
            override fun getChaincodeName(): String = "stub"
            override fun getContractName(): Optional<String> = Optional.empty()

            override fun newProposal(transactionName: String): Proposal.Builder {
                callCount.incrementAndGet()
                throw StatusRuntimeException(Status.INTERNAL.withDescription("stub failure"))
            }

            override fun submitTransaction(name: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
        }

        // parallelism = 3: keep the three items concurrently scheduled so the test actually
        // exercises sibling behavior under the supervisor scope, not sequential execution.
        val client = FabricGatewayClient(stubBuilder(countingContract), parallelism = 3)

        val outcomes = client.invoke(
            channelId = "ch",
            chaincodeId = "cc",
            invokeArgsList = listOf(
                InvokeArgs("fn", "x"),
                InvokeArgs("fn", "y"),
                InvokeArgs("fn", "z"),
            ),
        )

        // All 3 items must have been attempted (not cancelled due to first failure).
        assertThat(callCount.get()).isEqualTo(3)
        assertThat(outcomes).hasSize(3)
    }

    @Test
    suspend fun `parallelism caps the number of concurrently executing items`() {
        val inFlight = AtomicInteger(0)
        val maxInFlight = AtomicInteger(0)
        val calls = AtomicInteger(0)
        val gaugingContract: Contract = object : Contract {
            override fun getChaincodeName(): String = "stub"
            override fun getContractName(): Optional<String> = Optional.empty()

            override fun newProposal(transactionName: String): Proposal.Builder {
                calls.incrementAndGet()
                val now = inFlight.incrementAndGet()
                maxInFlight.updateAndGet { seen -> maxOf(seen, now) }
                try {
                    // Hold the slot long enough for the other items to pile up on the dispatcher.
                    Thread.sleep(50)
                } finally {
                    inFlight.decrementAndGet()
                }
                throw StatusRuntimeException(Status.UNAVAILABLE.withDescription("stub: no gRPC"))
            }

            override fun submitTransaction(name: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
        }

        val client = FabricGatewayClient(stubBuilder(gaugingContract), parallelism = 2)

        val outcomes = client.invoke(
            channelId = "ch",
            chaincodeId = "cc",
            invokeArgsList = (1..8).map { InvokeArgs("fn", "arg$it") },
        )

        // Every item ran, none were lost to the cap...
        assertThat(calls.get()).isEqualTo(8)
        assertThat(outcomes).hasSize(8)
        // ...and at no point did more than `parallelism` items execute at once.
        assertThat(maxInFlight.get()).isLessThanOrEqualTo(2)
    }

    @Test
    suspend fun `query evaluates each item on the shared dispatcher and returns the payloads in order`() {
        val evaluatingContract: Contract = object : Contract {
            override fun getChaincodeName(): String = "stub"
            override fun getContractName(): Optional<String> = Optional.empty()

            override fun newProposal(transactionName: String): Proposal.Builder =
                throw UnsupportedOperationException("query must not endorse")

            override fun submitTransaction(name: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: String): ByteArray = ByteArray(0)
            override fun submitTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
            override fun evaluateTransaction(name: String): ByteArray = "eval:$name".toByteArray()
            override fun evaluateTransaction(name: String, vararg args: String): ByteArray =
                "eval:$name:${args.joinToString(",")}".toByteArray()
            override fun evaluateTransaction(name: String, vararg args: ByteArray): ByteArray = ByteArray(0)
        }

        val client = FabricGatewayClient(stubBuilder(evaluatingContract), parallelism = 2)

        val results = client.query(
            channelId = "ch",
            chaincodeId = "cc",
            invokeArgsList = listOf(
                InvokeArgs("fnA", "x"),
                InvokeArgs("fnB", "y", "z"),
            ),
        )

        assertThat(results).containsExactly("eval:fnA:x", "eval:fnB:y,z")
    }
}
