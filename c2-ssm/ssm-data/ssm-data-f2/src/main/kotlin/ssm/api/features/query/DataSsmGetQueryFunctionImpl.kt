package ssm.api.features.query

import f2.dsl.fnc.invokeWith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.api.extentions.toDataSsm
import ssm.chaincode.dsl.model.uri.burst
import ssm.chaincode.dsl.query.SsmGetQuery
import ssm.chaincode.dsl.query.SsmGetQueryFunction
import ssm.data.dsl.features.query.DataSsmGetQueryDTO
import ssm.data.dsl.features.query.DataSsmGetQueryFunction
import ssm.data.dsl.features.query.DataSsmGetQueryResult
import ssm.data.dsl.features.query.DataSsmGetQueryResultDTO

class DataSsmGetQueryFunctionImpl(
    private val ssmGetQueryFunction: SsmGetQueryFunction
) : DataSsmGetQueryFunction {

    override suspend fun invoke(msgs: Flow<DataSsmGetQueryDTO>): Flow<DataSsmGetQueryResultDTO> =
        msgs.map { payload ->
            SsmGetQuery(
                chaincodeUri = payload.ssmUri.burst().chaincodeUri,
                name = payload.ssmUri.burst().ssmName,
            ).invokeWith(ssmGetQueryFunction)
                .item
                ?.toDataSsm(payload.ssmUri.burst())
                .let(::DataSsmGetQueryResult)

        }
}
