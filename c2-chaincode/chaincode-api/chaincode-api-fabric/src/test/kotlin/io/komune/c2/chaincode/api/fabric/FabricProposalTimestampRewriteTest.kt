package io.komune.c2.chaincode.api.fabric

import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import io.komune.c2.chaincode.dsl.ChaincodeId
import io.komune.c2.chaincode.dsl.ChannelId
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.fabric.client.Contract
import org.hyperledger.fabric.protos.common.ChannelHeader
import org.hyperledger.fabric.protos.common.Header
import org.hyperledger.fabric.protos.common.HeaderType
import org.hyperledger.fabric.protos.common.SignatureHeader
import org.hyperledger.fabric.protos.gateway.ProposedTransaction
import org.hyperledger.fabric.protos.peer.ChaincodeProposalPayload
import org.hyperledger.fabric.protos.peer.SignedProposal
import org.junit.jupiter.api.Test
import org.hyperledger.fabric.protos.peer.Proposal as PeerProposal

/**
 * Unit tests for [FabricGatewayClient.rewriteProposalTimestamp] — the pure protobuf transform behind the
 * business-timestamp feature. Builds a ProposedTransaction shaped like the one fabric-gateway produces,
 * rewrites its ChannelHeader.timestamp, and asserts the timestamp changed while every other field
 * (transaction id, channel id, chaincode payload, signature-header nonce/creator) is preserved.
 */
class FabricProposalTimestampRewriteTest {

    private val client = FabricGatewayClient(
        object : FabricGatewayBuilder() {
            override fun contracts(channelId: ChannelId, chaincodeId: ChaincodeId): List<Contract> = emptyList()
        },
        parallelism = 1,
    )

    private val signatureHeader = SignatureHeader.newBuilder()
        .setCreator(ByteString.copyFromUtf8("creator-msp-identity"))
        .setNonce(ByteString.copyFromUtf8("nonce-deadbeef"))
        .build()

    private val payload = ChaincodeProposalPayload.newBuilder()
        .setInput(ByteString.copyFromUtf8("chaincode-invocation-spec"))
        .build()

    private fun proposedTransaction(
        txId: String,
        channelId: String,
        originalEpochSeconds: Long,
    ): ProposedTransaction {
        val channelHeader = ChannelHeader.newBuilder()
            .setType(HeaderType.ENDORSER_TRANSACTION.number)
            .setTxId(txId)
            .setChannelId(channelId)
            .setEpoch(0)
            .setTimestamp(Timestamp.newBuilder().setSeconds(originalEpochSeconds).setNanos(0).build())
            .build()
        val header = Header.newBuilder()
            .setChannelHeader(channelHeader.toByteString())
            .setSignatureHeader(signatureHeader.toByteString())
            .build()
        val innerProposal = PeerProposal.newBuilder()
            .setHeader(header.toByteString())
            .setPayload(payload.toByteString())
            .build()
        val signedProposal = SignedProposal.newBuilder()
            .setProposalBytes(innerProposal.toByteString())
            .setSignature(ByteString.copyFromUtf8("stale-signature-over-old-bytes"))
            .build()
        return ProposedTransaction.newBuilder()
            .setProposal(signedProposal)
            .setTransactionId(txId)
            .build()
    }

    private fun ProposedTransaction.channelHeader(): ChannelHeader {
        val inner = PeerProposal.parseFrom(proposal.proposalBytes)
        return ChannelHeader.parseFrom(Header.parseFrom(inner.header).channelHeader)
    }

    @Test
    fun `rewrites channel-header timestamp to the given epoch millis with sub-second precision`() {
        val original = proposedTransaction(txId = "tx-abc", channelId = "ch1", originalEpochSeconds = 999L)

        // 2021-06-15T00:00:00.123Z
        val rewritten = ProposedTransaction.parseFrom(
            client.rewriteProposalTimestamp(original.toByteArray(), 1_623_715_200_123L)
        )

        val ts = rewritten.channelHeader().timestamp
        assertThat(ts.seconds).isEqualTo(1_623_715_200L)
        assertThat(ts.nanos).isEqualTo(123_000_000)
    }

    @Test
    fun `preserves transaction id, channel id, payload and signature header`() {
        val original = proposedTransaction(txId = "tx-xyz", channelId = "sandbox", originalEpochSeconds = 42L)

        val rewritten = ProposedTransaction.parseFrom(
            client.rewriteProposalTimestamp(original.toByteArray(), 1_600_000_000_000L)
        )

        // Top-level transaction id is untouched.
        assertThat(rewritten.transactionId).isEqualTo("tx-xyz")

        val inner = PeerProposal.parseFrom(rewritten.proposal.proposalBytes)
        val header = Header.parseFrom(inner.header)
        val channelHeader = ChannelHeader.parseFrom(header.channelHeader)

        // Channel-header identity fields are preserved — only the timestamp moved.
        assertThat(channelHeader.txId).isEqualTo("tx-xyz")
        assertThat(channelHeader.channelId).isEqualTo("sandbox")
        assertThat(channelHeader.type).isEqualTo(HeaderType.ENDORSER_TRANSACTION.number)
        // Signature header (nonce + creator, what the tx id hashes over) is byte-identical.
        assertThat(header.signatureHeader).isEqualTo(signatureHeader.toByteString())
        // Chaincode payload (the actual invocation args) is byte-identical.
        assertThat(inner.payload).isEqualTo(payload.toByteString())
    }

    @Test
    fun `clears a pre-existing signature so endorse re-signs the rewritten bytes`() {
        val original = proposedTransaction(txId = "tx-signed", channelId = "ch", originalEpochSeconds = 1L)
        assertThat(original.proposal.signature.isEmpty).isFalse()

        val rewritten = ProposedTransaction.parseFrom(
            client.rewriteProposalTimestamp(original.toByteArray(), 1_623_715_200_000L)
        )

        assertThat(rewritten.proposal.signature.isEmpty).isTrue()
    }

    @Test
    fun `exact-second timestamp yields zero nanos`() {
        val original = proposedTransaction(txId = "tx-1", channelId = "ch", originalEpochSeconds = 1L)

        val rewritten = ProposedTransaction.parseFrom(
            client.rewriteProposalTimestamp(original.toByteArray(), 1_623_715_200_000L)
        )

        val ts = rewritten.channelHeader().timestamp
        assertThat(ts.seconds).isEqualTo(1_623_715_200L)
        assertThat(ts.nanos).isEqualTo(0)
    }
}
