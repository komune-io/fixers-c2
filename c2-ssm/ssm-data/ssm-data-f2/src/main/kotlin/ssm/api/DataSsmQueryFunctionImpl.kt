package ssm.api

import ssm.api.features.query.DataChaincodeListQueryFunctionImp
import ssm.api.features.query.DataSsmGetQueryFunctionImpl
import ssm.api.features.query.DataSsmListQueryFunctionImp
import ssm.api.features.query.DataSsmSessionGetQueryFunctionImpl
import ssm.api.features.query.DataSsmSessionListQueryFunctionImpl
import ssm.api.features.query.DataSsmSessionLogGetQueryFunctionImpl
import ssm.api.features.query.DataSsmSessionLogListQueryFunctionImpl
import ssm.api.features.query.internal.DataSsmSessionConvertFunctionImpl
import ssm.chaincode.dsl.SsmChaincodeQueries
import ssm.chaincode.f2.ChaincodeSsmQueriesImpl
import ssm.couchdb.dsl.SsmCouchDbQueries
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl
import ssm.data.dsl.SsmApiQueryFunctions
import ssm.data.dsl.config.DataSsmConfig
import ssm.data.dsl.features.query.DataChaincodeListQueryFunction
import ssm.data.dsl.features.query.DataSsmGetQueryFunction
import ssm.data.dsl.features.query.DataSsmListQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogListQueryFunction

class DataSsmQueryFunctionImpl(
    private val config: DataSsmConfig,
    private val ssmChaincodeQueries: SsmChaincodeQueries = ChaincodeSsmQueriesImpl(config.batch, config.chaincode),
    private val couchDbSsmQueries: SsmCouchDbQueries = CouchdbSsmQueriesFunctionImpl(config.couchdb)
) : SsmApiQueryFunctions {

    override fun dataChaincodeListQueryFunction(): DataChaincodeListQueryFunction =
        DataChaincodeListQueryFunctionImp(
            couchdbChaincodeListQueryFunction = couchDbSsmQueries.couchdbChaincodeListQueryFunction()
        )

    override fun dataSsmListQueryFunction(): DataSsmListQueryFunction =
        DataSsmListQueryFunctionImp(couchdbSsmListQueryFunction = couchDbSsmQueries.couchdbSsmListQueryFunction())

    override fun dataSsmGetQueryFunction(): DataSsmGetQueryFunction =
        DataSsmGetQueryFunctionImpl(
            ssmGetQueryFunction = ssmChaincodeQueries.ssmGetQueryFunction()
        )

    override fun dataSsmSessionListQueryFunction(): DataSsmSessionListQueryFunction =
        DataSsmSessionListQueryFunctionImpl(
            dataSsmSessionConvertFunctionImpl = dataSsmSessionConvertFunctionImpl(),
            couchdbSsmSessionStateListQueryFunction = couchDbSsmQueries.couchdbSsmSessionStateListQueryFunction()
        )

    override fun dataSsmSessionGetQueryFunction(): DataSsmSessionGetQueryFunction =
        DataSsmSessionGetQueryFunctionImpl(
            ssmGetSessionQueryFunction = ssmChaincodeQueries.ssmGetSessionQueryFunction(),
            dataSsmSessionConvertFunction = dataSsmSessionConvertFunctionImpl(),
        )

    override fun dataSsmSessionLogListQueryFunction(): DataSsmSessionLogListQueryFunction =
        DataSsmSessionLogListQueryFunctionImpl(
            ssmGetSessionLogsQueryFunction = ssmChaincodeQueries.ssmGetSessionLogsQueryFunction(),
            ssmGetTransactionQueryFunction = ssmChaincodeQueries.ssmGetTransactionQueryFunction()
        )

    override fun dataSsmSessionLogGetQueryFunction(): DataSsmSessionLogGetQueryFunction =
        DataSsmSessionLogGetQueryFunctionImpl(
            ssmGetSessionLogsQueryFunction = ssmChaincodeQueries.ssmGetSessionLogsQueryFunction(),
            ssmGetTransactionQueryFunction = ssmChaincodeQueries.ssmGetTransactionQueryFunction()
        )

    private fun dataSsmSessionConvertFunctionImpl(): DataSsmSessionConvertFunctionImpl =
        DataSsmSessionConvertFunctionImpl(
            ssmGetSessionLogsQueryFunction = ssmChaincodeQueries.ssmGetSessionLogsQueryFunction(),
            ssmGetTransactionQueryFunction = ssmChaincodeQueries.ssmGetTransactionQueryFunction()
        )
}
