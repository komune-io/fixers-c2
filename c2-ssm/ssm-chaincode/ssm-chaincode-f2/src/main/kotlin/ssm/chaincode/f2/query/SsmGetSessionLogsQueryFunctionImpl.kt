package ssm.chaincode.f2.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ssm.chaincode.dsl.query.SsmGetSessionLogsQuery
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryFunction
import ssm.chaincode.dsl.query.SsmGetSessionLogsQueryResult
import ssm.sdk.core.SsmQueryService

class SsmGetSessionLogsQueryFunctionImpl(
	private val queryService: SsmQueryService
): SsmGetSessionLogsQueryFunction  {

	// TODO CHANGE THAT should better use flow
	override suspend fun invoke(
		msgs: Flow<SsmGetSessionLogsQuery>
	): Flow<SsmGetSessionLogsQueryResult> = msgs.map { payload ->
		queryService.log(payload.chaincodeUri, payload.sessionName)
			.let { logs ->
				SsmGetSessionLogsQueryResult(
					ssmName = payload.ssmName,
					sessionName = payload.sessionName,
					logs = logs
				)
			}
	}
}
