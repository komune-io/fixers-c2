package ssm.chaincode.dsl.config

import f2.dsl.fnc.operators.BATCH_DEFAULT_CONCURRENCY
import f2.dsl.fnc.operators.BATCH_DEFAULT_SIZE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SsmBatchPropertiesTest {

    @Test
    fun `defaults align with the f2 batch defaults`() {
        val properties = SsmBatchProperties()
        assertThat(properties.timeout).isEqualTo(120000)
        assertThat(properties.size).isEqualTo(BATCH_DEFAULT_SIZE)
        assertThat(properties.concurrency).isEqualTo(BATCH_DEFAULT_CONCURRENCY)
    }

    @Test
    fun `toBatch carries size and concurrency over`() {
        val batch = SsmBatchProperties(timeout = 5, size = 1024, concurrency = 7).toBatch()
        assertThat(batch.size).isEqualTo(1024)
        assertThat(batch.concurrency).isEqualTo(7)
    }
}
