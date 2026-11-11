package io.komune.c2.chaincode.api.config.utils

import java.net.MalformedURLException
import java.net.URL

interface HasKeystore {
    val keystore: String

    @Throws(MalformedURLException::class)
    fun getKeystoreAsUrl(cryptoBase: String): URL =
        FileUtils.getUrl(path = cryptoBase, resource = keystore)
}
