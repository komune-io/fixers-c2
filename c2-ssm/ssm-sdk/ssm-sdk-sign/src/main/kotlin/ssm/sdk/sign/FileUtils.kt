package ssm.sdk.sign

import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.nio.channels.FileChannel
import java.nio.file.Path

object FileUtils {
	const val FILE = "file:"

	@Throws(MalformedURLException::class)
	fun getFile(filename: String): File {
		val url = getUrl(filename)
		return File(url.file)
	}

	@Throws(MalformedURLException::class)
	fun getUrl(filename: String): URL {
		return if (filename.startsWith(FILE)) {
			URI(filename).toURL()
		} else {
			Thread.currentThread().contextClassLoader.getResource(filename)!!
		}
	}

	@Throws(IOException::class)
	fun getReader(filename: String): Reader {
		val url = getUrl(filename)
		return InputStreamReader(url.openStream())
	}

	@Throws(IOException::class)
	fun sameContent(file1: Path, file2: Path): Boolean {
		val mbba = FileChannel.open(file1).use { fc1 ->
			fc1.map(FileChannel.MapMode.READ_ONLY, 0, fc1.size())
		}
		val mbbb = FileChannel.open(file2).use { fc2 ->
			fc2.map(FileChannel.MapMode.READ_ONLY, 0, fc2.size())
		}
		return mbba.equals(mbbb)
	}
}
