package ssm.api.features.query

import f2.dsl.fnc.invokeWith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import ssm.api.extentions.toDataSsm
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.couchdb.dsl.query.CouchdbSsmListQuery
import ssm.couchdb.dsl.query.CouchdbSsmListQueryFunction
import ssm.data.dsl.features.query.DataSsmListQuery
import ssm.data.dsl.features.query.DataSsmListQueryFunction
import ssm.data.dsl.features.query.DataSsmListQueryResult
import ssm.data.dsl.model.DataSsm

class DataSsmListQueryFunctionImp(
	private val couchdbSsmListQueryFunction: CouchdbSsmListQueryFunction
): DataSsmListQueryFunction {

	private suspend fun CouchdbSsmListQuery.queryEachChannel(chaincode: ChaincodeUri): Flow<DataSsm> =
		invokeWith(couchdbSsmListQueryFunction)
			.let { result ->
				result.page.items.asFlow().map {
					it.toDataSsm(chaincode)
				}
			}.filterNotNull()

	override suspend fun invoke(msg: Flow<DataSsmListQuery>): Flow<DataSsmListQueryResult> =
		msg.map { payload ->
			payload.chaincodes
				.asFlow()
				.flatMapConcat { chaincodeUri ->
					CouchdbSsmListQuery(
						chaincodeId = chaincodeUri.chaincodeId,
						channelId = chaincodeUri.channelId,
						pagination = null
					).queryEachChannel(chaincodeUri)
				}.let {
					DataSsmListQueryResult(it.toList())
				}

		}
}

