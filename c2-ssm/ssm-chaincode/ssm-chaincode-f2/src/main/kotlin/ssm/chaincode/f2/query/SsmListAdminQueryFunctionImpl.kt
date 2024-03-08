package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmListAdminQuery
import ssm.chaincode.dsl.query.SsmListAdminQueryFunction
import ssm.chaincode.dsl.query.SsmListAdminResult
import ssm.sdk.core.SsmQueryService

class SsmListAdminQueryFunctionImpl(
	private val queryService: SsmQueryService
) : SsmListAdminQueryFunction {

	override suspend fun invoke(msg: Flow<SsmListAdminQuery>): Flow<SsmListAdminResult> = msg.map { payload ->
		queryService.listAdmins(payload.chaincodeUri).let { items ->
			SsmListAdminResult(items.toTypedArray())
		}
	}
}
