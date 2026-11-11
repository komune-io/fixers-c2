package ssm.sync.sdk

import ssm.api.DataSsmQueryFunctionImpl
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl
import ssm.data.dsl.config.DataSsmConfig

object SsmSyncF2Builder {
    fun build(dataConfig: DataSsmConfig): SyncSsmCommandFunctionImpl {
        val couchDbDatabaseGetChangesQueryFunction = CouchdbSsmQueriesFunctionImpl(dataConfig.couchdb)
        val dataSsmQueryFunctionImpl = DataSsmQueryFunctionImpl(dataConfig)
        return SyncSsmCommandFunctionImpl(
            dataSsmGetQueryFunction = dataSsmQueryFunctionImpl.dataSsmGetQueryFunction(),
            dataSsmSessionLogListQueryFunction = dataSsmQueryFunctionImpl.dataSsmSessionLogListQueryFunction(),
            couchdbSsmSessionStateGetQueryFunction
                = couchDbDatabaseGetChangesQueryFunction.couchdbSsmSessionStateGetQueryFunction(),
            couchdbDatabaseGetChangesQueryFunction
                = couchDbDatabaseGetChangesQueryFunction.couchdbDatabaseGetChangesQueryFunction()
        )
    }
}
