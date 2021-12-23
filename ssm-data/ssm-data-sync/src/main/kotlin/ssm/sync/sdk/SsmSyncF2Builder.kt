package ssm.sync.sdk

import ssm.api.DataSsmQueryFunctionImpl
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl
import ssm.data.dsl.config.DataSsmConfig


object SsmSyncF2Builder {
	fun build(dataConfig: DataSsmConfig): SyncSsmF2Impl {
		val couchDbDatabaseGetChangesQueryFunction = CouchdbSsmQueriesFunctionImpl(dataConfig.couchdb)
		val dataSsmQueryFunctionImpl = DataSsmQueryFunctionImpl(dataConfig)
		return SyncSsmF2Impl(
			dataSsmGetQueryFunction = dataSsmQueryFunctionImpl.dataSsmGetQueryFunction(),
			dataSsmSessionLogListQueryFunction = dataSsmQueryFunctionImpl.dataSsmSessionLogListQueryFunction(),
			couchdbSsmSessionStateGetQueryFunction = couchDbDatabaseGetChangesQueryFunction.couchdbSsmSessionStateGetQueryFunction(),
			couchdbDatabaseGetChangesQueryFunction = couchDbDatabaseGetChangesQueryFunction.couchdbDatabaseGetChangesQueryFunction()
		)
	}
}