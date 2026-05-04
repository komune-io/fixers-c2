package io.komune.c2.chaincode.api.config.utils

import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.Properties


interface HasTlsCacerts {
    val tlsCacerts: String

    @Throws(MalformedURLException::class)
    fun getTlsCacertsAsUrl(cryptoBase: String): URL {
        return FileUtils.getUrl(cryptoBase, tlsCacerts)
    }

    @Throws(IOException::class)
    fun getPeerTlsProperties(cryptoBase: String): Properties {
        val prop = Properties()
        prop.setProperty("allowAllHostNames", "true")
        val path = getTlsCacertsAsUrl(cryptoBase)
        prop.setProperty("pemFile", path.file)
        return prop
    }
}
