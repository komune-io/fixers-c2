package io.komune.c2.chaincode.api.config.utils

import java.io.File
import java.net.URISyntaxException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FileUtilsTest {

	@TempDir
	lateinit var tempDir: File

	@Test
	fun `getUrl should combine path and resource with trailing slash`() {
		val url = FileUtils.getUrl("file:/tmp/config/", "fabric.json")
		assertThat(url.toString()).isEqualTo("file:/tmp/config/fabric.json")
	}

	@Test
	fun `getUrl should add missing trailing slash between path and resource`() {
		val url = FileUtils.getUrl("file:/tmp/config", "fabric.json")
		assertThat(url.toString()).isEqualTo("file:/tmp/config/fabric.json")
	}

	@Test
	fun `getUrl should resolve file prefixed resource as url`() {
		val url = FileUtils.getUrl("file:/tmp/config/fabric.json")
		assertThat(url.protocol).isEqualTo("file")
		assertThat(url.path).isEqualTo("/tmp/config/fabric.json")
	}

	@Test
	fun `getUrl should resolve classpath resource`() {
		val url = FileUtils.getUrl("test-resource.txt")
		assertThat(url).isNotNull()
		assertThat(url.readText()).contains("test-content")
	}

	@Test
	fun `getResource should resolve classpath resource`() {
		val url = FileUtils.getResource("test-resource.txt")
		assertThat(url.readText()).contains("test-content")
	}

	@Test
	fun `asFileReader should read a regular file`() {
		val file = File(tempDir, "config.json")
		file.writeText("""{"key":"value"}""")
		val content = file.toURI().toURL().asFileReader().use { it.readText() }
		assertThat(content).isEqualTo("""{"key":"value"}""")
	}

	@Test
	fun `asFileReader should read the first file of a directory`() {
		File(tempDir, "only.json").writeText("""{"only":true}""")
		val content = tempDir.toURI().toURL().asFileReader().use { it.readText() }
		assertThat(content).isEqualTo("""{"only":true}""")
	}

	@Test
	fun `asFileReader should fail on empty directory`() {
		val emptyDir = File(tempDir, "empty").apply { mkdirs() }
		assertThatThrownBy { emptyDir.toURI().toURL().asFileReader() }
			.isInstanceOf(URISyntaxException::class.java)
	}
}
