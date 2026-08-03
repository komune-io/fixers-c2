package io.komune.c2.chaincode.api.config.utils

import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import tools.jackson.databind.node.NullNode

class JsonUtilsTest {

	data class Sample(val name: String, val value: Int)

	@TempDir
	lateinit var tempDir: File

	@Test
	fun `toJson serializes an object`() {
		assertThat(JsonUtils.toJson(Sample("test", 42))).isEqualTo("""{"name":"test","value":42}""")
	}

	@Test
	fun `toNode parses a json string`() {
		val node = JsonUtils.toNode("""{"name":"test"}""")
		assertThat(node.get("name").asString()).isEqualTo("test")
	}

	@Test
	fun `toNode returns a null node on blank input`() {
		assertThat(JsonUtils.toNode(" ")).isEqualTo(NullNode.instance)
	}

	@Test
	fun `valueToTree converts an object to a node`() {
		val node = JsonUtils.valueToTree(Sample("test", 7))
		assertThat(node.get("value").asInt()).isEqualTo(7)
	}

	@Test
	fun `toObject reads a typed object from an url`() {
		val file = File(tempDir, "sample.json")
		file.writeText("""{"name":"from-file","value":1}""")

		val byClass = JsonUtils.toObject(file.toURI().toURL(), Sample::class.java)
		assertThat(byClass.name).isEqualTo("from-file")

		val byReified: Sample = JsonUtils.toObject(file.toURI().toURL())
		assertThat(byReified.value).isEqualTo(1)
	}
}
