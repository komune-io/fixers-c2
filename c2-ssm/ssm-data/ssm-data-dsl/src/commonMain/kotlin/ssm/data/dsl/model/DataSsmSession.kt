package ssm.data.dsl.model

import io.komune.c2.chaincode.dsl.Transaction
import io.komune.c2.chaincode.dsl.TransactionDTO
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.uri.SsmUri

@JsExport
@JsName("DataSsmSessionDTO")
interface DataSsmSessionDTO {
	/**
	 * uri of the ssm
	 * @example "ssm:peerId?:channelId:chaincodeId"
	 */
	val ssmUri: SsmUri

	/**
	 * Identifier of the session
	 * @example [ssm.chaincode.dsl.model.SsmSession.session]
	 */
	val sessionName: SessionName

	/**
	 * The state of an SSM session
	 */
	val state: DataSsmSessionStateDTO

	/**
	 * The channel this session was instantiated in
	 */
	val channel: DataChannelDTO

	/**
	 * Transaction that initiated the state of the session
	 */
	val transaction: TransactionDTO?
	val transactions: List<TransactionDTO>
}

/**
 * @d2 model
 * @parent [ssm.data.dsl.DataSsmD2Model]
 * Represents an SSM session with metadata
 */
@Serializable
@JsExport
@JsName("DataSsmSession")
class DataSsmSession(
    override val sessionName: SessionName,
    override val state: DataSsmSessionState,
    override val channel: DataChannel,
    override val transaction: Transaction?,
    override val ssmUri: SsmUri,
    override val transactions: List<TransactionDTO>,
) : DataSsmSessionDTO
