package ssm.couchdb.client

import com.ibm.cloud.cloudant.v1.model.ChangesResultItem
import com.ibm.cloud.sdk.core.util.GsonSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.couchdb.dsl.model.DocType

class ChangesResultItemExtensionTest {

    private fun item(id: String): ChangesResultItem =
        GsonSingleton.getGson().fromJson("""{"id":"$id"}""", ChangesResultItem::class.java)

    @Test
    fun `maps each known document id prefix to its doc type`() {
        assertThat(item("SSM:CarDealership").getDocType()).isEqualTo(DocType.Ssm)
        assertThat(item("STATE:deal20181201").getDocType()).isEqualTo(DocType.State)
        assertThat(item("USER:sarah").getDocType()).isEqualTo(DocType.User)
        assertThat(item("ADMIN:chuck").getDocType()).isEqualTo(DocType.Admin)
        assertThat(item("GRANT:chuck").getDocType()).isEqualTo(DocType.Grant)
    }

    @Test
    fun `returns null for an unknown document id prefix`() {
        assertThat(item("OTHER:doc").getDocType()).isNull()
        assertThat(item("ssm:lowercase").getDocType()).isNull()
    }
}
