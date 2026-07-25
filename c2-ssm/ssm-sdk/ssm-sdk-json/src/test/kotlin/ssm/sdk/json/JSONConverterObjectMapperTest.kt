package ssm.sdk.json

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent

internal class JSONConverterObjectMapperTest {

    private val converter = JSONConverterObjectMapper()

    @Test
    fun `toObject returns null on blank input instead of failing to parse`() {
        assertThat(converter.toObject(Agent::class.java, "")).isNull()
        assertThat(converter.toObject(Agent::class.java, "   ")).isNull()
    }

    @Test
    fun `toObject deserializes a json payload`() {
        val agent = converter.toObject(Agent::class.java, """{"name":"Adam","pub":"AQID"}""")
        assertThat(agent).isEqualTo(Agent("Adam", byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `toCompletableObject mirrors toObject including the blank guard`() {
        assertThat(converter.toCompletableObject(Agent::class.java, "")).isNull()
        assertThat(converter.toCompletableObject(Agent::class.java, """{"name":"Eve","pub":"AQID"}"""))
            .isEqualTo(Agent("Eve", byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `toCompletableObjects returns an empty list on blank input`() {
        assertThat(converter.toCompletableObjects(Agent::class.java, "")).isEmpty()
        assertThat(converter.toCompletableObjects(Agent::class.java, "  ")).isEmpty()
    }

    @Test
    fun `toCompletableObjects deserializes a json array`() {
        val values = converter.toCompletableObjects(String::class.java, """["a","b"]""")
        assertThat(values).containsExactly("a", "b")
    }
}
