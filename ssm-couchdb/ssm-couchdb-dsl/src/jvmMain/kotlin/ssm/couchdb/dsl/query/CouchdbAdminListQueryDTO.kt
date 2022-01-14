package ssm.couchdb.dsl.query

import f2.dsl.cqrs.Event
import f2.dsl.cqrs.Query
import ssm.chaincode.dsl.model.SsmAgentDTO
import ssm.chaincode.dsl.model.uri.ChaincodeUriDTO

actual interface CouchdbAdminListQueryDTO : Query {
	/**
	 * The unique id of a chaincode.
	 */
	actual val chaincodeUri: ChaincodeUriDTO
}

actual interface CouchdbAdminListQueryResultDTO : Event {
	actual val items: List<SsmAgentDTO>
}
