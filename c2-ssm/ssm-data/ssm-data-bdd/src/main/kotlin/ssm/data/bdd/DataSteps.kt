package ssm.data.bdd

import f2.dsl.fnc.invokeWith
import io.cucumber.java8.En
import ssm.api.DataSsmQueryFunctionImpl
import ssm.bdd.config.SsmBddConfig
import ssm.bdd.config.SsmQueryStep
import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.SsmName
import ssm.chaincode.dsl.model.SsmSessionStateDTO
import ssm.chaincode.dsl.model.SsmSessionStateLog
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.f2.ChaincodeSsmQueriesImpl
import ssm.couchdb.dsl.query.CouchdbAdminListQuery
import ssm.couchdb.dsl.query.CouchdbUserListQuery
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl
import ssm.data.dsl.config.DataSsmConfig
import ssm.data.dsl.features.query.DataSsmListQuery
import ssm.data.dsl.features.query.DataSsmSessionGetQuery
import ssm.data.dsl.features.query.DataSsmSessionListQuery

class DataSteps : SsmQueryStep(), En {

    val ssmDataConfig: DataSsmConfig = SsmBddConfig.Data.config
    val dataSsmQueryFunctionImpl =
        DataSsmQueryFunctionImpl(ssmDataConfig, ChaincodeSsmQueriesImpl(batch, ssmDataConfig.chaincode))
    val couchdbSsmQueriesFunctionImpl = CouchdbSsmQueriesFunctionImpl(ssmDataConfig.couchdb)

    init {
        prepareSteps()
    }

    override suspend fun getSession(ssmUri: SsmUri, sessionName: SessionName): SsmSessionStateDTO {
        val session = DataSsmSessionGetQuery(
            sessionName = sessionName,
            ssmUri = ssmUri,
        ).invokeWith(dataSsmQueryFunctionImpl.dataSsmSessionGetQueryFunction())
            .item
        return checkNotNull(session) { "Session $sessionName not found" }
            .state
            .details
    }

    override suspend fun logSession(ssmUri: SsmUri, sessionName: SessionName): List<SsmSessionStateLog> =
        bag.clientQuery.log(ssmUri.chaincodeUri, sessionName)

    override suspend fun listSessions(ssmUri: SsmUri): List<SessionName> {
        return DataSsmSessionListQuery(
            ssmUri = ssmUri
        ).invokeWith(dataSsmQueryFunctionImpl.dataSsmSessionListQueryFunction())
            .items
            .map { it.state.details.session }
    }

    override suspend fun listSsm(): List<SsmName> {
        return DataSsmListQuery(
            chaincodes = listOf(bag.chaincodeUri)
        ).invokeWith(dataSsmQueryFunctionImpl.dataSsmListQueryFunction())
            .items
            .map { it.ssm.name }
    }

    override suspend fun listUsers(): List<String> {
        return CouchdbUserListQuery(chaincodeUri = bag.chaincodeUri)
            .invokeWith(couchdbSsmQueriesFunctionImpl.couchdbUserListQueryFunction())
            .items
            .map { it.name }
    }

    override suspend fun listAdmins(): List<String> {
        return CouchdbAdminListQuery(chaincodeUri = bag.chaincodeUri)
            .invokeWith(couchdbSsmQueriesFunctionImpl.couchdbAdminListQueryFunction())
            .items
            .map { it.name }
    }

    companion object {
        const val GLUE = "ssm.data.bdd"
    }
}
