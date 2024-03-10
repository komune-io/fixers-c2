package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmGetUserFunction
import ssm.chaincode.dsl.query.SsmGetUserQuery
import ssm.chaincode.dsl.query.SsmGetUserResult
import ssm.sdk.core.SsmQueryService

class SsmGetUserFunctionImpl(
	private val queryService: SsmQueryService
): SsmGetUserFunction {

	override suspend fun invoke(msgs: Flow<SsmGetUserQuery>): Flow<SsmGetUserResult> = msgs.map { payload ->
		queryService.getAgent(payload.chaincodeUri, payload.name).let { items ->
			SsmGetUserResult(items)
		}
	}
}
