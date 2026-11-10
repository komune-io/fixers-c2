package ssm.couchdb.f2.query

import ssm.couchdb.bdd.TestConfig
import ssm.couchdb.f2.CouchdbSsmQueriesFunctionImpl

open class FunctionTestBase {
    val queries = CouchdbSsmQueriesFunctionImpl(TestConfig.dbConfig)
}
