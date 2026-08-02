package ssm.sdk.sign

import java.io.File
import java.net.MalformedURLException
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class FileUtilsTest {

	companion object {
		private const val SAMPLE_RESOURCE = "files/sample.txt"
		private const val SAMPLE_CONTENT = "sample content\n"
	}

	@Test
	fun getUrlShouldResolveClasspathResource() {
		val url = FileUtils.getUrl(SAMPLE_RESOURCE)
		Assertions.assertThat(url.path).endsWith("files/sample.txt")
	}

	@Test
	fun getUrlShouldResolveFilePrefixedPath() {
		val file = File.createTempFile("file-utils", ".txt")
		try {
			file.writeText(SAMPLE_CONTENT)
			val url = FileUtils.getUrl(file.toURI().toString())
			Assertions.assertThat(url.protocol).isEqualTo("file")
			Assertions.assertThat(File(url.file).readText()).isEqualTo(SAMPLE_CONTENT)
		} finally {
			file.delete()
		}
	}

	@Test
	fun getUrlShouldFailOnInvalidFileUri() {
		Assertions.assertThatThrownBy { FileUtils.getUrl("file:^invalid uri") }
			.isInstanceOf(Exception::class.java)
	}

	@Test
	fun getFileShouldReturnReadableFile() {
		val file = FileUtils.getFile(SAMPLE_RESOURCE)
		Assertions.assertThat(file).exists()
		Assertions.assertThat(file.readText()).isEqualTo(SAMPLE_CONTENT)
	}

	@Test
	@Throws(MalformedURLException::class)
	fun getReaderShouldReadResourceContent() {
		val content = FileUtils.getReader(SAMPLE_RESOURCE).use { it.readText() }
		Assertions.assertThat(content).isEqualTo(SAMPLE_CONTENT)
	}

	@Test
	fun sameContentShouldReturnTrueForIdenticalFiles() {
		val file1 = createTempFile("same-content", ".txt").apply { writeText("identical") }
		val file2 = createTempFile("same-content", ".txt").apply { writeText("identical") }
		try {
			Assertions.assertThat(FileUtils.sameContent(file1, file2)).isTrue()
		} finally {
			file1.toFile().delete()
			file2.toFile().delete()
		}
	}

	@Test
	fun sameContentShouldReturnFalseForDifferentFiles() {
		val file1 = createTempFile("same-content", ".txt").apply { writeText("content a") }
		val file2 = createTempFile("same-content", ".txt").apply { writeText("content b") }
		try {
			Assertions.assertThat(FileUtils.sameContent(file1, file2)).isFalse()
		} finally {
			file1.toFile().delete()
			file2.toFile().delete()
		}
	}
}
