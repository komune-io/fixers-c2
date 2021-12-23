package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmGetQuery
import ssm.chaincode.dsl.query.SsmGetQueryFunction
import ssm.chaincode.dsl.query.SsmGetResult
import ssm.sdk.core.SsmQueryService

class SsmGetQueryFunctionImpl(
	private val queryService: SsmQueryService
) : SsmGetQueryFunction {

	override suspend fun invoke(msg: Flow<SsmGetQuery>): Flow<SsmGetResult> = msg.map { payload ->
		queryService.getSsm(payload.name).let(::SsmGetResult)
	}
}