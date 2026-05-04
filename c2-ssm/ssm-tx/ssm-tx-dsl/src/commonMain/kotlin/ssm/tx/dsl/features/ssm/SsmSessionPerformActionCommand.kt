package ssm.tx.dsl.features.ssm

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.TransactionId
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.SsmContext
import ssm.tx.dsl.features.SsmCommandDTO
import ssm.tx.dsl.features.SsmCommandResultDTO

/**
 * Performs a state transition for a given session
 * @d2 function
 * @parent [ssm.tx.dsl.SsmTxD2Command]
 * @title Perform Transition
 * @order 50
 */
typealias SsmTxSessionPerformActionFunction = F2Function<SsmSessionPerformActionCommand, SsmSessionPerformActionResult>

/**
 * @d2 command
 * @parent [SsmTxSessionPerformActionFunction]
 * @title Perform Transition: Parameters
 */
class SsmSessionPerformActionCommand(
    override val chaincodeUri: ChaincodeUriDTO,
    /**
	 * The name of the signer
	 */
	val signerName: AgentName,
    /**
	 * Transition to perform
	 * @example [ssm.chaincode.dsl.model.SsmTransition.action]
	 */
	val action: String,

    /**
	 * Information about the transition
	 */
	val context: SsmContext,
) : SsmCommandDTO

/**
 * @d2 event
 * @parent [SsmTxSessionPerformActionFunction]
 * @title Perform Transition: Response
 */
class SsmSessionPerformActionResult(
	override val transactionId: TransactionId
) : SsmCommandResultDTO
