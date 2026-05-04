package ssm.sdk.json

import java.time.Instant
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ssm.chaincode.dsl.model.Agent
import tools.jackson.core.type.TypeReference

internal class JsonUtilsTest {

	@Test
	fun `handle LocalDateTime`() {
		val time = LocalDateTime.now()
		val json = JsonUtils.toJson(WithLocalDateTime(time))
		val data = JsonUtils.toObject(json, WithLocalDateTime::class.java)
		assertThat(data.time).isEqualTo(time)
	}

	@Test
	fun `handle LocalDateTime as map key`() {
		val time = LocalDateTime.now()
		val json = JsonUtils.toJson(WithLocalDateTimeAsMapKey(mapOf(time to "test")))
		val data = JsonUtils.toObject(json, WithLocalDateTimeAsMapKey::class.java)
		assertThat(data.times).containsKey(time)
	}

	@Test
	fun `deserialize Long from ISO string`() {
		val json = """{"timestamp": "2026-02-01T11:37:10.231Z"}"""
		val data = JsonUtils.toObject(json, WithTimestamp::class.java)
		assertThat(data.timestamp).isEqualTo(Instant.parse("2026-02-01T11:37:10.231Z").toEpochMilli())
	}

	@Test
	fun `deserialize Long from epoch millis`() {
		val json = """{"timestamp": 1738410430231}"""
		val data = JsonUtils.toObject(json, WithTimestamp::class.java)
		assertThat(data.timestamp).isEqualTo(1738410430231L)
	}

	@Test
	fun `deserialize Long from numeric string`() {
		val json = """{"timestamp": "1738410430231"}"""
		val data = JsonUtils.toObject(json, WithTimestamp::class.java)
		assertThat(data.timestamp).isEqualTo(1738410430231L)
	}

	@Test
	fun `serialize should omit null values`() {
		val data = WithNullableField("value", null)
		val json = JsonUtils.toJson(data)
		assertThat(json).isEqualTo("""{"name":"value"}""")
		assertThat(json).doesNotContain("optional")
	}

	@Test
	fun `toObject with TypeReference deserializes list`() {
		val json = """[{"name":"item1"},{"name":"item2"}]"""
		val result: List<SimpleItem> = JsonUtils.toObject(json, object : TypeReference<List<SimpleItem>>() {})
		assertThat(result).hasSize(2)
		assertThat(result[0].name).isEqualTo("item1")
		assertThat(result[1].name).isEqualTo("item2")
	}

	@Test
	fun `toObject with reified type deserializes list`() {
		val json = """[{"name":"first"},{"name":"second"}]"""
		val result: List<SimpleItem> = JsonUtils.toObject(json)
		assertThat(result).hasSize(2)
		assertThat(result[0].name).isEqualTo("first")
		assertThat(result[1].name).isEqualTo("second")
	}

	@Test
	fun `toObject with reified type and type inference`() {
		val json = """{"name":"inferred","value":42}"""
		val result: ItemWithValue = JsonUtils.toObject(json)
		assertThat(result.name).isEqualTo("inferred")
		assertThat(result.value).isEqualTo(42)
	}

	@Test
	fun `deserialize Agent from JSON`() {
		val json = """{"name":"Adam","pub":"AQID"}"""
		val result = JsonUtils.toObject(json, Agent::class.java)
		assertThat(result.name).isEqualTo("Adam")
		assertThat(result.pub).isEqualTo(byteArrayOf(1, 2, 3))
	}

	@Test
	fun `roundtrip Agent serialization`() {
		val original = Agent("Adam", byteArrayOf(1, 2, 3))
		val json = JsonUtils.toJson(original)
		val result = JsonUtils.toObject(json, Agent::class.java)
		assertThat(result.name).isEqualTo(original.name)
		assertThat(result.pub).isEqualTo(original.pub)
	}
}

data class WithLocalDateTime(val time: LocalDateTime)

data class WithLocalDateTimeAsMapKey(val times: Map<LocalDateTime, String>)

data class WithTimestamp(val timestamp: Long)

data class WithNullableField(val name: String, val optional: String?)

data class SimpleItem(val name: String)

data class ItemWithValue(val name: String, val value: Int)
