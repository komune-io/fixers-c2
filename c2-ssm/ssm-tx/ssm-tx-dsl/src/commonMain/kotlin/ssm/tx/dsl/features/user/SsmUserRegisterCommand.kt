package ssm.tx.dsl.features.user

import f2.dsl.fnc.F2Function
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.TransactionId
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.tx.dsl.features.SsmCommandDTO
import ssm.tx.dsl.features.SsmCommandResultDTO

/**
 * A new user is registered by an admin, Agent structure is signed with the admin private key.
 * @d2 function
 * @parent [ssm.tx.dsl.SsmTxD2Command]
 * @title Register User
 */
typealias SsmTxUserRegisterFunction = F2Function<SsmUserRegisterCommand, SsmUserRegisteredResult>

/**
 * @d2 command
 * @parent [SsmTxUserRegisterFunction]
 * @title Register User: Parameters
 */
class SsmUserRegisterCommand(
    override val chaincodeUri: ChaincodeUriDTO,
    /**
	 * The name of the signer
	 */
	val signerName: AgentName,
    /**
	 * Admin signing the transaction
	 */
//	val signerAdmin: SignerAdmin,
    /**
	 * Initial user of the SSM
	 */
	val agent: Agent,
) : SsmCommandDTO

/**
 * @d2 event
 * @parent [SsmTxUserRegisterFunction]
 * @title Register User: Response
 */
class SsmUserRegisteredResult(
	override val transactionId: TransactionId
) : SsmCommandResultDTO
