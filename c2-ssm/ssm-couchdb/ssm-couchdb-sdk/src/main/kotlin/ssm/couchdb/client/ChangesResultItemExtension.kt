package ssm.couchdb.client

import com.ibm.cloud.cloudant.v1.model.ChangesResultItem
import ssm.couchdb.dsl.model.DocType

fun ChangesResultItem.getDocType(): DocType<*>? {
    return when {
        id.startsWith("SSM") -> DocType.Ssm
        id.startsWith("STATE") -> DocType.State
        id.startsWith("USER") -> DocType.User
        id.startsWith("ADMIN") -> DocType.Admin
        id.startsWith("GRANT") -> DocType.Grant
        else -> null
    }
}
