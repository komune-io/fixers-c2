package io.komune.c2.chaincode.api.config.utils

import java.io.IOException
import java.net.URL
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue


object JsonUtils {
    val mapper: ObjectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()

    fun toJson(obj: Any?): String {
        return mapper.writeValueAsString(obj)
    }

    @Throws(IOException::class)
    fun <T> toObject(value: URL?, clazz: Class<T>?): T {
        return mapper.readValue(value!!.openStream(), clazz)
    }
    @Throws(IOException::class)
    inline fun <reified T> toObject(value: URL): T {
        return mapper.readValue(value.openStream())
    }
}
