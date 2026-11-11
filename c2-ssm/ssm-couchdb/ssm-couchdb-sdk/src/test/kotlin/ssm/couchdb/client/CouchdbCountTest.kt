package ssm.couchdb.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/** The count a CouchDB view row carries. Pure value reading: no server needed. */
class CouchdbCountTest {

    @Test
    fun `no row counts zero and a number is the count`() {
        assertThat(CouchdbSsmClient.countOf(view = "indexType", value = null)).isZero()
        assertThat(CouchdbSsmClient.countOf(view = "indexType", value = 12.0)).isEqualTo(12)
    }

    @Test
    fun `a value that is not a number is an error, not a silent zero`() {
        assertThatThrownBy { val _ = CouchdbSsmClient.countOf(view = "indexType", value = "12") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("indexType")
    }
}
