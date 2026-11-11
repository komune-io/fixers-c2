package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import ssm.chaincode.dsl.query.SsmGetTransactionQuery
import ssm.chaincode.dsl.query.SsmGetTransactionQueryFunction
import ssm.chaincode.dsl.query.SsmGetTransactionQueryResult
import ssm.sdk.core.GetTransactionQuery
import ssm.sdk.core.SsmQueryService

class SsmGetTransactionQueryFunctionImpl(
    private val queryService: SsmQueryService
) : SsmGetTransactionQueryFunction {

    // F2Function declares invoke suspend: an override cannot drop it.
    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun invoke(msgs: Flow<SsmGetTransactionQuery>): Flow<SsmGetTransactionQueryResult> = flow {
        val transactionQuery = msgs.map { GetTransactionQuery(it.chaincodeUri, it.id)  }.toList()
        val transactions = queryService.getTransactions(transactionQuery).map(::SsmGetTransactionQueryResult)
        emitAll(transactions.asFlow())
    }
}
