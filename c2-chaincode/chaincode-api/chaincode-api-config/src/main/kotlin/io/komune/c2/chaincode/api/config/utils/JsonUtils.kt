package io.komune.c2.chaincode.api.config.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.net.URL


object JsonUtils {
    val mapper: ObjectMapper = jacksonObjectMapper()

    @Throws(com.fasterxml.jackson.core.JsonProcessingException::class)
    fun toJson(obj: Any?): String {
        return mapper.writeValueAsString(obj)
    }

    @Throws(IOException::class)
    fun <T> toObject(value: URL?, clazz: Class<T>?): T {
        return mapper.readValue(value, clazz)
    }
    @Throws(IOException::class)
    inline fun <reified T> toObject(value: URL): T {
        return mapper.readValue(value)
    }
}
