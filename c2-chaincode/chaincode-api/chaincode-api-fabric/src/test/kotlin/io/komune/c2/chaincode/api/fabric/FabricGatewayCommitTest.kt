package io.komune.c2.chaincode.api.fabric

import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import io.grpc.CallOptions
import io.grpc.Status as GrpcStatus
import io.grpc.StatusRuntimeException
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.invoke.InvokeArgs
import java.util.Optional
import java.util.function.UnaryOperator
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.fabric.client.Contract
import org.hyperledger.fabric.client.EndorseException
import org.hyperledger.fabric.client.Gateway
import org.hyperledger.fabric.client.Proposal
import org.hyperledger.fabric.client.Status
import org.hyperledger.fabric.client.SubmittedTransaction
import org.hyperledger.fabric.client.Transaction
import org.hyperledger.fabric.protos.common.ChannelHeader
import org.hyperledger.fabric.protos.common.Header
import org.hyperledger.fabric.protos.common.SignatureHeader
import org.hyperledger.fabric.protos.gateway.ProposedTransaction
import org.hyperledger.fabric.protos.peer.ChaincodeProposalPayload
import org.hyperledger.fabric.protos.peer.SignedProposal
import org.hyperledger.fabric.protos.peer.TxValidationCode
import org.junit.jupiter.api.Test
import org.hyperledger.fabric.protos.peer.Proposal as PeerProposal

/**
 * Drives FabricGatewayClient.invoke through a full successful commit using in-memory stubs (no live
 * Fabric). This exercises the endorse -> submit -> commit happy path of commitTransaction — including the
 * business-timestamp rewrite branch — which the throwing-stub tests in FabricGatewayInvokeClientTest never
 * reach.
 */
class FabricGatewayCommitTest {

    private fun proposedTxBytes(txId: String = "tx-1", channelId: String = "ch"): ByteArray {
        val channelHeader = ChannelHeader.newBuilder()
            .setTxId(txId).setChannelId(channelId)
            .setTimestamp(Timestamp.newBuilder().setSeconds(1L).build())
            .build()
        val signatureHeader = SignatureHeader.newBuilder().setNonce(ByteString.copyFromUtf8("n")).build()
        val header = Header.newBuilder()
            .setChannelHeader(channelHeader.toByteString())
            .setSignatureHeader(signatureHeader.toByteString())
            .build()
        val payload = ChaincodeProposalPayload.newBuilder().setInput(ByteString.copyFromUtf8("in")).build()
        val inner = PeerProposal.newBuilder()
            .setHeader(header.toByteString())
            .setPayload(payload.toByteString())
            .build()
        return ProposedTransaction.newBuilder()
            .setProposal(SignedProposal.newBuilder().setProposalBytes(inner.toByteString()).build())
            .setTransactionId(txId)
            .build()
            .toByteArray()
    }

    private fun clientFor(status: Status, gateway: Gateway? = null) =
        FabricGatewayClient(builder(stubContract(stubProposal(proposedTxBytes(), status)), gateway), parallelism = 1)

    private fun stubStatus(successful: Boolean, block: Long, code: TxValidationCode = TxValidationCode.VALID) =
        object : Status {
            override fun getTransactionId() = "tx-1"
            override fun getBlockNumber() = block
            override fun getCode() = code
            override fun isSuccessful() = successful
        }

    private fun stubSubmitted(status: Status) = object : SubmittedTransaction {
        override fun getResult() = "ok".toByteArray()
        override fun getTransactionId() = "tx-1"
        override fun getBytes() = ByteArray(0)
        override fun getDigest() = ByteArray(0)
        override fun getStatus(options: UnaryOperator<CallOptions>?) = status
    }

    private fun stubTransaction(status: Status) = object : Transaction {
        override fun getResult() = "ok".toByteArray()
        override fun getTransactionId() = "tx-1"
        override fun getBytes() = ByteArray(0)
        override fun getDigest() = ByteArray(0)
        override fun submit(options: UnaryOperator<CallOptions>?) = getResult()
        override fun submitAsync(options: UnaryOperator<CallOptions>?) = stubSubmitted(status)
    }

    private fun stubProposal(bytes: ByteArray, status: Status) = object : Proposal {
        override fun getTransactionId() = "tx-1"
        override fun getBytes() = bytes
        override fun getDigest() = ByteArray(0)
        override fun evaluate(options: UnaryOperator<CallOptions>?) = ByteArray(0)
        override fun endorse(options: UnaryOperator<CallOptions>?) = stubTransaction(status)
    }

    private fun stubContract(proposal: Proposal) = object : Contract {
        override fun getChaincodeName() = "cc"
        override fun getContractName(): Optional<String> = Optional.empty()
        override fun newProposal(transactionName: String): Proposal.Builder = object : Proposal.Builder {
            override fun addArguments(vararg args: ByteArray): Proposal.Builder = this
            override fun addArguments(vararg args: String): Proposal.Builder = this
            override fun putAllTransient(transientData: MutableMap<String, ByteArray>): Proposal.Builder = this
            override fun putTransient(key: String, value: ByteArray): Proposal.Builder = this
            override fun putTransient(key: String, value: String): Proposal.Builder = this
            override fun setEndorsingOrganizations(vararg mspids: String): Proposal.Builder = this
            override fun build(): Proposal = proposal
        }
        override fun submitTransaction(name: String) = ByteArray(0)
        override fun submitTransaction(name: String, vararg args: String) = ByteArray(0)
        override fun submitTransaction(name: String, vararg args: ByteArray) = ByteArray(0)
        override fun evaluateTransaction(name: String) = ByteArray(0)
        override fun evaluateTransaction(name: String, vararg args: String) = ByteArray(0)
        override fun evaluateTransaction(name: String, vararg args: ByteArray) = ByteArray(0)
    }

    private fun stubGateway(recreated: Proposal, onNewProposal: (ByteArray) -> Unit = {}) = object : Gateway {
        override fun newProposal(proposalBytes: ByteArray): Proposal {
            onNewProposal(proposalBytes)
            return recreated
        }
        override fun getIdentity() = error("unused")
        override fun getNetwork(networkName: String) = error("unused")
        override fun newSignedProposal(p: ByteArray, s: ByteArray) = error("unused")
        override fun newSignedTransaction(t: ByteArray, s: ByteArray) = error("unused")
        override fun newTransaction(t: ByteArray) = error("unused")
        override fun newSignedCommit(b: ByteArray, s: ByteArray) = error("unused")
        override fun newCommit(b: ByteArray) = error("unused")
        override fun newSignedChaincodeEventsRequest(b: ByteArray, s: ByteArray) = error("unused")
        override fun newChaincodeEventsRequest(b: ByteArray) = error("unused")
        override fun newSignedBlockEventsRequest(b: ByteArray, s: ByteArray) = error("unused")
        override fun newBlockEventsRequest(b: ByteArray) = error("unused")
        override fun newSignedFilteredBlockEventsRequest(b: ByteArray, s: ByteArray) = error("unused")
        override fun newFilteredBlockEventsRequest(b: ByteArray) = error("unused")
        override fun newSignedBlockAndPrivateDataEventsRequest(b: ByteArray, s: ByteArray) = error("unused")
        override fun newBlockAndPrivateDataEventsRequest(b: ByteArray) = error("unused")
        override fun close() {}
    }

    private fun builder(contract: Contract, gateway: Gateway? = null) = object : FabricGatewayBuilder() {
        override fun contracts(channelId: ChannelId, chaincodeId: ChaincodeId) = listOf(contract)
        override fun gateway(channelId: ChannelId): Gateway = gateway ?: error("gateway() not stubbed")
    }

    @Test
    fun `successful commit without timestamp yields a Committed outcome`() = runTest {
        val status = stubStatus(successful = true, block = 7L)
        val client = clientFor(status)

        val outcomes = client.invoke("ch", "cc", listOf(InvokeArgs("fn", "a")), listOf("m1"))

        val committed = outcomes.single() as TxOutcome.Committed
        assertThat(committed.msgId).isEqualTo("m1")
        assertThat(committed.transactionId).isEqualTo("tx-1")
        assertThat(committed.blockNumber).isEqualTo(7L)
    }

    @Test
    fun `successful commit with timestamp goes through the rewrite path and commits`() = runTest {
        val status = stubStatus(successful = true, block = 9L)
        val built = stubProposal(proposedTxBytes(), status)      // returned by contract.build(); bytes fed to rewrite
        val recreated = stubProposal(proposedTxBytes(), status)  // returned by gateway.newProposal(rewrittenBytes)
        var rewrittenBytes: ByteArray? = null
        val client = FabricGatewayClient(
            builder(stubContract(built), stubGateway(recreated) { rewrittenBytes = it }),
            parallelism = 1,
        )

        val outcomes = client.invoke(
            "ch", "cc",
            listOf(InvokeArgs("fn", "a", timestamp = 1_623_715_200_000L)),
            listOf("m1"),
        )

        assertThat(outcomes.single()).isInstanceOf(TxOutcome.Committed::class.java)
        // The bytes handed to Gateway.newProposal must carry the rewritten (business) timestamp — proving the
        // rewrite branch ran, not just that some commit happened.
        val proposedTx = ProposedTransaction.parseFrom(requireNotNull(rewrittenBytes))
        val inner = PeerProposal.parseFrom(proposedTx.proposal.proposalBytes)
        val timestamp = ChannelHeader.parseFrom(Header.parseFrom(inner.header).channelHeader).timestamp
        assertThat(timestamp.seconds).isEqualTo(1_623_715_200L)
        assertThat(timestamp.nanos).isZero()
    }

    @Test
    fun `unsuccessful validation code yields a non-committed outcome`() = runTest {
        val status = stubStatus(successful = false, block = 3L, code = TxValidationCode.MVCC_READ_CONFLICT)
        val client = clientFor(status)

        val outcomes = client.invoke("ch", "cc", listOf(InvokeArgs("fn", "a")), listOf("m1"))

        assertThat(outcomes.single()).isNotInstanceOf(TxOutcome.Committed::class.java)
    }

    @Test
    fun `endorse failure yields a Rejected outcome`() = runTest {
        val proposal = object : Proposal {
            override fun getTransactionId() = "tx-1"
            override fun getBytes() = proposedTxBytes()
            override fun getDigest() = ByteArray(0)
            override fun evaluate(options: UnaryOperator<CallOptions>?) = ByteArray(0)
            override fun endorse(options: UnaryOperator<CallOptions>?): Transaction =
                throw EndorseException("tx-1", StatusRuntimeException(GrpcStatus.ABORTED))
        }
        val client = FabricGatewayClient(builder(stubContract(proposal)), parallelism = 1)

        val outcomes = client.invoke("ch", "cc", listOf(InvokeArgs("fn", "a")), listOf("m1"))

        assertThat(outcomes.single()).isInstanceOf(TxOutcome.Rejected::class.java)
    }

    @Test
    fun `submit failure yields an Indeterminate outcome`() = runTest {
        val throwingTx = object : Transaction {
            override fun getResult() = ByteArray(0)
            override fun getTransactionId() = "tx-1"
            override fun getBytes() = ByteArray(0)
            override fun getDigest() = ByteArray(0)
            override fun submit(options: UnaryOperator<CallOptions>?) = ByteArray(0)
            override fun submitAsync(options: UnaryOperator<CallOptions>?): SubmittedTransaction =
                throw IllegalStateException("submit boom")
        }
        val proposal = object : Proposal {
            override fun getTransactionId() = "tx-1"
            override fun getBytes() = proposedTxBytes()
            override fun getDigest() = ByteArray(0)
            override fun evaluate(options: UnaryOperator<CallOptions>?) = ByteArray(0)
            override fun endorse(options: UnaryOperator<CallOptions>?) = throwingTx
        }
        val client = FabricGatewayClient(builder(stubContract(proposal)), parallelism = 1)

        val outcomes = client.invoke("ch", "cc", listOf(InvokeArgs("fn", "a")), listOf("m1"))

        assertThat(outcomes.single()).isInstanceOf(TxOutcome.Indeterminate::class.java)
    }
}
