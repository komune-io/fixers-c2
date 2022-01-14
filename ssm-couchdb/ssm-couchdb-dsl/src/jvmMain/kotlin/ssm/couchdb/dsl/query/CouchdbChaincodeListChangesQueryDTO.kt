package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import ssm.chaincode.dsl.model.uri.ChaincodeUriDTO

actual interface CouchdbChaincodeListQueryDTO : Query

actual interface CouchdbChaincodeListQueryResultDTO : Event {
	actual val items: List<ChaincodeUriDTO>
}
