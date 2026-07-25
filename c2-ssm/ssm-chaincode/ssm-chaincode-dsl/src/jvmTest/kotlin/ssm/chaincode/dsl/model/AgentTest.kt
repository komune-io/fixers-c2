package ssm.chaincode.dsl.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Agent hand-writes equals/hashCode because `pub` is a ByteArray (reference
 * equality by default) — these tests lock in content-based comparison.
 */
class AgentTest {

    @Test
    fun `agents with equal key content are equal even across distinct arrays`() {
        val a = Agent("Adam", byteArrayOf(1, 2, 3))
        val b = Agent("Adam", byteArrayOf(1, 2, 3))
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `agents differing by key content or name are not equal`() {
        val a = Agent("Adam", byteArrayOf(1, 2, 3))
        assertThat(a).isNotEqualTo(Agent("Adam", byteArrayOf(9, 9)))
        assertThat(a).isNotEqualTo(Agent("Eve", byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `an agent equals itself and never equals null or another type`() {
        val a = Agent("Adam", byteArrayOf(1))
        assertThat(a).isEqualTo(a)
        assertThat(a).isNotEqualTo(null)
        assertThat(a).isNotEqualTo("Adam")
    }
}
