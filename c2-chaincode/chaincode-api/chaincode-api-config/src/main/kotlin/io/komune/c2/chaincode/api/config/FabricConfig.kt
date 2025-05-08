package io.komune.c2.chaincode.api.config

import io.komune.c2.chaincode.api.config.properties.NetworkProperties
import io.komune.c2.chaincode.api.config.utils.FileUtils
import io.komune.c2.chaincode.api.config.utils.JsonUtils
import java.io.IOException
import java.net.URL


class FabricConfig(
    val network: NetworkProperties
) {

    companion object {
        @kotlin.Throws(IOException::class)
        fun loadFromFile(filename: String): FabricConfig {
            val file: URL = FileUtils.getUrl(filename)
            return JsonUtils.toObject<FabricConfig>(file)
        }
    }
}
