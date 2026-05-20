package ssm.tx.dsl.features.ssm

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.TransactionId
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmSession
import ssm.tx.dsl.features.SsmCommandDTO
import ssm.tx.dsl.features.SsmCommandResultDTO

/**
 * Create a session that instantiates a given ssm
 * @d2 function
 * @parent [ssm.tx.dsl.SsmTxD2Command]
 * @title Start Session
 * @order 40
 */
typealias SsmTxSessionStartFunction = F2Function<SsmSessionStartCommand, SsmSessionStartResult>

/**
 * @d2 command
 * @parent [SsmTxSessionStartFunction]
 * @title Start Session: Parameters
 */
class SsmSessionStartCommand(
    override val chaincodeUri: ChaincodeUriDTO,
    /**
	 * The name of the signer
	 */
	val signerName: AgentName,
    /**
	 * Session to start
	 */
	val session: SsmSession,
) : SsmCommandDTO

/**
 * @d2 event
 * @parent [SsmTxSessionStartFunction]
 * @title Start Session: Response
 */
class SsmSessionStartResult(
    override val transactionId: TransactionId,
) : SsmCommandResultDTO
