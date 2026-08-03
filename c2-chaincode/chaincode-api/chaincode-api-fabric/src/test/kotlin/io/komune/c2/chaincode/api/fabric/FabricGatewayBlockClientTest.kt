package io.komune.c2.chaincode.api.fabric

import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import io.grpc.CallOptions
import java.util.Optional
import java.util.function.UnaryOperator
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.fabric.client.BlockAndPrivateDataEventsRequest
import org.hyperledger.fabric.client.BlockEventsRequest
import org.hyperledger.fabric.client.ChaincodeEvent
import org.hyperledger.fabric.client.ChaincodeEventsRequest
import org.hyperledger.fabric.client.CloseableIterator
import org.hyperledger.fabric.client.Commit
import org.hyperledger.fabric.client.Contract
import org.hyperledger.fabric.client.FilteredBlockEventsRequest
import org.hyperledger.fabric.client.Gateway
import org.hyperledger.fabric.client.Network
import org.hyperledger.fabric.client.Proposal
import org.hyperledger.fabric.client.identity.Identity
import org.hyperledger.fabric.protos.common.Block
import org.hyperledger.fabric.protos.common.BlockData
import org.hyperledger.fabric.protos.common.BlockHeader
import org.hyperledger.fabric.protos.common.BlockchainInfo
import org.hyperledger.fabric.protos.common.ChannelHeader
import org.hyperledger.fabric.protos.common.Envelope
import org.hyperledger.fabric.protos.common.Header
import org.hyperledger.fabric.protos.common.Payload
import org.hyperledger.fabric.protos.common.SignatureHeader
import org.hyperledger.fabric.protos.msp.SerializedIdentity
import org.hyperledger.fabric.protos.peer.BlockAndPrivateData
import org.hyperledger.fabric.protos.peer.FilteredBlock
import org.hyperledger.fabric.protos.peer.ProcessedTransaction
import org.junit.jupiter.api.Test

class FabricGatewayBlockClientTest {

	private class FakeContract(
		private val responses: Map<String, ByteArray>,
	) : Contract {
		val calls = mutableListOf<Pair<String, List<String>>>()

		override fun getChaincodeName(): String = "qscc"
		override fun getContractName(): Optional<String> = Optional.empty()

		override fun evaluateTransaction(name: String): ByteArray = evaluate(name, emptyList())
		override fun evaluateTransaction(name: String, vararg args: String): ByteArray = evaluate(name, args.toList())
		override fun evaluateTransaction(name: String, vararg args: ByteArray): ByteArray =
			throw UnsupportedOperationException()

		private fun evaluate(name: String, args: List<String>): ByteArray {
			calls.add(name to args)
			return responses.getValue(name)
		}

		override fun submitTransaction(name: String): ByteArray = throw UnsupportedOperationException()
		override fun submitTransaction(name: String, vararg args: String): ByteArray =
			throw UnsupportedOperationException()
		override fun submitTransaction(name: String, vararg args: ByteArray): ByteArray =
			throw UnsupportedOperationException()
		override fun newProposal(name: String): Proposal.Builder = throw UnsupportedOperationException()
	}

	private class FakeNetwork(private val contract: Contract) : Network {
		override fun getContract(chaincodeName: String): Contract = contract
		override fun getContract(chaincodeName: String, contractName: String): Contract = contract
		override fun getName(): String = "sandbox"
		override fun getChaincodeEvents(
			chaincodeName: String,
			options: UnaryOperator<CallOptions>,
		): CloseableIterator<ChaincodeEvent> = throw UnsupportedOperationException()
		override fun newChaincodeEventsRequest(chaincodeName: String): ChaincodeEventsRequest.Builder =
			throw UnsupportedOperationException()
		override fun getBlockEvents(options: UnaryOperator<CallOptions>): CloseableIterator<Block> =
			throw UnsupportedOperationException()
		override fun newBlockEventsRequest(): BlockEventsRequest.Builder = throw UnsupportedOperationException()
		override fun getFilteredBlockEvents(options: UnaryOperator<CallOptions>): CloseableIterator<FilteredBlock> =
			throw UnsupportedOperationException()
		override fun newFilteredBlockEventsRequest(): FilteredBlockEventsRequest.Builder =
			throw UnsupportedOperationException()
		override fun getBlockAndPrivateDataEvents(
			options: UnaryOperator<CallOptions>,
		): CloseableIterator<BlockAndPrivateData> = throw UnsupportedOperationException()
		override fun newBlockAndPrivateDataEventsRequest(): BlockAndPrivateDataEventsRequest.Builder =
			throw UnsupportedOperationException()
	}

	private class FakeGateway(private val network: Network) : Gateway {
		override fun getIdentity(): Identity = throw UnsupportedOperationException()
		override fun getNetwork(networkName: String): Network = network
		override fun newSignedProposal(bytes: ByteArray, signature: ByteArray): Proposal =
			throw UnsupportedOperationException()
		override fun newProposal(bytes: ByteArray): Proposal = throw UnsupportedOperationException()
		override fun newSignedTransaction(
			bytes: ByteArray,
			signature: ByteArray,
		): org.hyperledger.fabric.client.Transaction = throw UnsupportedOperationException()
		override fun newTransaction(bytes: ByteArray): org.hyperledger.fabric.client.Transaction =
			throw UnsupportedOperationException()
		override fun newSignedCommit(bytes: ByteArray, signature: ByteArray): Commit =
			throw UnsupportedOperationException()
		override fun newCommit(bytes: ByteArray): Commit = throw UnsupportedOperationException()
		override fun newSignedChaincodeEventsRequest(
			bytes: ByteArray,
			signature: ByteArray,
		): ChaincodeEventsRequest = throw UnsupportedOperationException()
		override fun newChaincodeEventsRequest(bytes: ByteArray): ChaincodeEventsRequest =
			throw UnsupportedOperationException()
		override fun newSignedBlockEventsRequest(bytes: ByteArray, signature: ByteArray): BlockEventsRequest =
			throw UnsupportedOperationException()
		override fun newBlockEventsRequest(bytes: ByteArray): BlockEventsRequest =
			throw UnsupportedOperationException()
		override fun newSignedFilteredBlockEventsRequest(
			bytes: ByteArray,
			signature: ByteArray,
		): FilteredBlockEventsRequest = throw UnsupportedOperationException()
		override fun newFilteredBlockEventsRequest(bytes: ByteArray): FilteredBlockEventsRequest =
			throw UnsupportedOperationException()
		override fun newSignedBlockAndPrivateDataEventsRequest(
			bytes: ByteArray,
			signature: ByteArray,
		): BlockAndPrivateDataEventsRequest = throw UnsupportedOperationException()
		override fun newBlockAndPrivateDataEventsRequest(bytes: ByteArray): BlockAndPrivateDataEventsRequest =
			throw UnsupportedOperationException()
		override fun close() = Unit
	}

	private class FakeGatewayBuilder(private val gatewayInstance: Gateway) : FabricGatewayBuilder() {
		override fun gateway(channelId: String): Gateway = gatewayInstance
	}

	private fun buildClient(responses: Map<String, ByteArray>): Pair<FabricGatewayBlockClient, FakeContract> {
		val contract = FakeContract(responses)
		val gateway = FakeGateway(FakeNetwork(contract))
		return FabricGatewayBlockClient(FakeGatewayBuilder(gateway)) to contract
	}

	private fun envelopeBytes(txId: String): ByteArray {
		val identity = SerializedIdentity.newBuilder()
			.setMspid("SandboxMSP")
			.setIdBytes(ByteString.copyFromUtf8("identity-cert"))
			.build()
		val signatureHeader = SignatureHeader.newBuilder()
			.setCreator(identity.toByteString())
			.setNonce(ByteString.copyFromUtf8("nonce"))
			.build()
		val channelHeader = ChannelHeader.newBuilder()
			.setTxId(txId)
			.setChannelId("sandbox")
			.setTimestamp(Timestamp.newBuilder().setSeconds(1_700_000_000).setNanos(5_000_000))
			.build()
		val payload = Payload.newBuilder()
			.setHeader(
				Header.newBuilder()
					.setChannelHeader(channelHeader.toByteString())
					.setSignatureHeader(signatureHeader.toByteString())
			)
			.build()
		return Envelope.newBuilder()
			.setPayload(payload.toByteString())
			.build()
			.toByteArray()
	}

	private fun blockBytes(blockNumber: Long, txId: String): ByteArray = Block.newBuilder()
		.setHeader(
			BlockHeader.newBuilder()
				.setNumber(blockNumber)
				.setPreviousHash(ByteString.copyFromUtf8("previous-hash"))
				.setDataHash(ByteString.copyFromUtf8("data-hash"))
		)
		.setData(BlockData.newBuilder().addData(ByteString.copyFrom(envelopeBytes(txId))))
		.build()
		.toByteArray()

	@Test
	fun `queryAllBlocksIds returns one id per block of the chain`() {
		val chainInfo = BlockchainInfo.newBuilder().setHeight(3).build().toByteArray()
		val (client, contract) = buildClient(mapOf("GetChainInfo" to chainInfo))

		val result = client.queryAllBlocksIds("sandbox")

		assertThat(result).containsExactly(0L, 1L, 2L)
		assertThat(contract.calls).containsExactly("GetChainInfo" to listOf("sandbox"))
	}

	@Test
	fun `queryBlockByNumber parses the block and its transactions`() {
		val (client, contract) = buildClient(mapOf("GetBlockByNumber" to blockBytes(4, "tx-1")))

		val block = client.queryBlockByNumber("sandbox", 4)

		assertThat(block.blockId).isEqualTo(4)
		assertThat(block.previousHash).isEqualTo("previous-hash".toByteArray())
		assertThat(block.dataHash).isEqualTo("data-hash".toByteArray())
		assertThat(block.transactions).hasSize(1)
		val transaction = block.transactions.first()
		assertThat(transaction.transactionId).isEqualTo("tx-1")
		assertThat(transaction.blockId).isEqualTo(4)
		assertThat(transaction.channelId).isEqualTo("sandbox")
		assertThat(transaction.timestamp).isEqualTo(1_700_000_000_000L + 5)
		assertThat(transaction.isValid).isTrue()
		assertThat(transaction.creator.mspid).isEqualTo("SandboxMSP")
		assertThat(transaction.creator.id).isEqualTo("identity-cert")
		assertThat(transaction.nonce).isEqualTo("nonce".toByteArray())
		assertThat(contract.calls).containsExactly("GetBlockByNumber" to listOf("sandbox", "4"))
	}

	@Test
	fun `queryBlockByTransactionId parses the block returned for the transaction`() {
		val (client, contract) = buildClient(mapOf("GetBlockByTxID" to blockBytes(6, "tx-9")))

		val block = client.queryBlockByTransactionId("sandbox", "tx-9")

		assertThat(block.blockId).isEqualTo(6)
		assertThat(block.transactions.map { it.transactionId }).containsExactly("tx-9")
		assertThat(contract.calls).containsExactly("GetBlockByTxID" to listOf("sandbox", "tx-9"))
	}

	@Test
	fun `queryTransactionById resolves the transaction and its block id`() {
		val processed = ProcessedTransaction.newBuilder()
			.setTransactionEnvelope(Envelope.parseFrom(envelopeBytes("tx-5")))
			.build()
			.toByteArray()
		val (client, contract) = buildClient(
			mapOf(
				"GetTransactionByID" to processed,
				"GetBlockByTxID" to blockBytes(8, "tx-5"),
			)
		)

		val transaction = client.queryTransactionById("sandbox", "tx-5")

		assertThat(transaction.transactionId).isEqualTo("tx-5")
		assertThat(transaction.blockId).isEqualTo(8)
		assertThat(transaction.creator.mspid).isEqualTo("SandboxMSP")
		assertThat(contract.calls.map { it.first })
			.containsExactly("GetTransactionByID", "GetBlockByTxID")
	}
}
