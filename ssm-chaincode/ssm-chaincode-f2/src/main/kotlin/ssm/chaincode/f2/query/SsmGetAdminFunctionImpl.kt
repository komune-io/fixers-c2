package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmGetAdminFunction
import ssm.chaincode.dsl.query.SsmGetAdminQuery
import ssm.chaincode.dsl.query.SsmGetAdminResult
import ssm.sdk.core.SsmQueryService

class SsmGetAdminFunctionImpl(private val queryService: SsmQueryService) : SsmGetAdminFunction {

	override suspend fun invoke(msg: Flow<SsmGetAdminQuery>): Flow<SsmGetAdminResult> = msg.map { payload ->
		val sessionState = queryService.getAdmin(payload.chaincodeUri, payload.name)
		SsmGetAdminResult(sessionState)
	}
}
