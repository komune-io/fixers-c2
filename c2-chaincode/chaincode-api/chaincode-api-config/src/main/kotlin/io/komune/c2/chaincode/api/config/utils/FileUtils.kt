package io.komune.c2.chaincode.api.config.utils

import java.io.File
import java.io.FileReader
import java.net.URISyntaxException
import java.net.URL


object FileUtils {
    const val FILE: String = "file:"

    fun getUrl(path: String, resource: String): URL {
        val normalizedPath = if(!path.endsWith("/"))
            "$path/"
        else
            path
        val fullPath = normalizedPath + resource
        return getUrl(fullPath)
    }

    fun getUrl(resource: String): URL {
        if (resource.startsWith(FILE)) {
            return URL(resource)
        }
        return getResource(resource)
    }

    fun getResource(resourceName: String): URL {
        val url =  Thread.currentThread().contextClassLoader ?: FileUtils::class.java.getClassLoader()
        return url.getResource(resourceName)!!
    }
}

fun URL.asFileReader(): FileReader {
    val url = this.toString().removePrefix("file:")
    val folder = File(url)
    return if (folder.isDirectory()) {
        folder.listFiles()?.firstOrNull() ?: throw URISyntaxException(url, "No files found")
    } else {
        folder
    }.let {
        FileReader(it)
    }
}

