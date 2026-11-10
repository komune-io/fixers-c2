package ssm.couchdb.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName

typealias ChangeEventId = String

/**
 * Information about a couchdb database.
 * TODO Use SHOUlD NOT BE H2 BU H3
 * @d2 query
 * @title Database
 * @parent [ssm.couchdb.dsl.CouchdbSsmD2Model]
 */
@JsExport
@JsName("DatabaseChangesDTO")
interface DatabaseChangesDTO {
    val changeEventId: ChangeEventId
    val docType: DocType<*>?
    val objectId: String
}

@JsExport
@JsName("DatabaseChanges")
class DatabaseChanges(
    override val changeEventId: ChangeEventId,
    override val docType: DocType<*>?,
    override val objectId: String
) : DatabaseChangesDTO
