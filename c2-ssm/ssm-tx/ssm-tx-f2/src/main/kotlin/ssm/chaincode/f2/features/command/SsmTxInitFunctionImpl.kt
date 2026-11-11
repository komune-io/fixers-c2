package ssm.chaincode.f2.features.command

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.burst
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.AgentName
import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.f2.utils.SsmException
import ssm.sdk.core.SsmQueryService
import ssm.sdk.core.SsmTxService
import ssm.sdk.dsl.CommandOutcome
import ssm.tx.dsl.features.ssm.SsmInitCommand
import ssm.tx.dsl.features.ssm.SsmInitdResult
import ssm.tx.dsl.features.ssm.SsmTxInitFunction

class SsmTxInitFunctionImpl(
    private val txService: SsmTxService,
    private val queryService: SsmQueryService,
): SsmTxInitFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun invoke(msgs: Flow<SsmInitCommand>): Flow<SsmInitdResult> = msgs.map { payload ->
        val retInitUser = initUser(payload.chaincodeUri.burst(), payload.agent, payload.signerName)
        val retInitSsm = initSsm(payload.chaincodeUri.burst(), payload.ssm, payload.signerName)
        val invoke = listOfNotNull(retInitUser, retInitSsm)
        SsmInitdResult(
            results = invoke.mapNotNull { it.transactionId }
        )
    }

    private suspend fun initSsm(chaincodeUri: ChaincodeUri, ssm: Ssm, signerName: AgentName): CommandOutcome? {
        return createIfNotExist(ssm,
            { queryService.getSsm(chaincodeUri, ssm.name) },
            { this.createSsm(chaincodeUri, it, signerName) }
        )
    }

    private suspend fun initUser(chaincodeUri: ChaincodeUri, user: Agent, signerName: AgentName): CommandOutcome? {
        return createIfNotExist(
            user,
            { queryService.getAgent(chaincodeUri, user.name) },
            { this.createUser(chaincodeUri, it, signerName) })
    }

    @Suppress("TooGenericExceptionCaught") // a failed create is fine when another writer created it meanwhile
    private suspend fun <T> createIfNotExist(
        objToCreate: T,
        getFnc: suspend () -> T?,
        create: suspend (T) -> CommandOutcome,
    ): CommandOutcome? {
        return if (getFnc() != null) {
            null
        } else {
            try {
                create(objToCreate)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (getFnc() != null) {
                    null
                } else {
                    throw e
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // every failure of the send is wrapped as an SsmException
    private suspend fun createSsm(chaincodeUri: ChaincodeUri, ssm: Ssm, signerName: AgentName): CommandOutcome {
        try {
            return txService.sendCreate(chaincodeUri, ssm, signerName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SsmException(e)
        }
    }

    @Suppress("TooGenericExceptionCaught") // every failure of the send is wrapped as an SsmException
    private suspend fun createUser(chaincodeUri: ChaincodeUri, agent: Agent, signerName: AgentName): CommandOutcome {
        try {
            return txService.sendRegisterUser(chaincodeUri, agent, signerName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SsmException(e)
        }
    }
}
