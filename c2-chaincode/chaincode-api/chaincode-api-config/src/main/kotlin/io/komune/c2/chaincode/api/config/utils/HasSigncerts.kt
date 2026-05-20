package io.komune.c2.chaincode.api.config.utils

import java.net.MalformedURLException
import java.net.URL

interface HasSigncerts {
    val signcerts: String

    @Throws(MalformedURLException::class)
    fun getSigncertsAsUrl(cryptoBase: String): URL {
        return FileUtils.getUrl(cryptoBase, signcerts)
    }
}
