package ssm.sdk.json

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.IOException
import java.io.Reader
import java.time.Instant
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.KotlinModule

object JsonUtils {

	private val timestampModule = SimpleModule("TimestampModule")
		.addDeserializer(Long::class.javaObjectType, LongTimestampDeserializer())
		.addDeserializer(Long::class.javaPrimitiveType, LongTimestampDeserializer())

	@PublishedApi
	internal val mapper: ObjectMapper = JsonMapper.builder()
		.addModule(KotlinModule.Builder().build())
		.addModule(timestampModule)
		.changeDefaultPropertyInclusion { JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, null) }
		.build()

	@Throws(IOException::class)
	fun <T> toJson(obj: T): String {
		return mapper.writeValueAsString(obj)
	}

	@Throws(IOException::class)
	fun <T> toObject(value: String, clazz: Class<T>): T {
		return mapper.readValue(value, clazz)
	}

	@Throws(IOException::class)
	fun <T> toObject(value: Reader, clazz: Class<T>): T {
		return mapper.readValue(value, clazz)
	}

	@Throws(IOException::class)
	fun <T> toObject(value: String, clazz: TypeReference<T>): T {
		return mapper.readValue(value, clazz)
	}

	@Throws(IOException::class)
	inline fun <reified T> toObject(value: String): T {
		return mapper.readValue(value, object : TypeReference<T>() {})
	}
}

/**
 * Deserializes Long values from ISO date strings or epoch milliseconds.
 * Handles the blockchain API returning timestamps as ISO strings.
 */
private class LongTimestampDeserializer : ValueDeserializer<Long>() {
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Long = when (p.currentToken()) {
		JsonToken.VALUE_NUMBER_INT -> p.longValue
		JsonToken.VALUE_STRING -> p.string.toLongOrNull() ?: Instant.parse(p.string).toEpochMilli()
		else -> throw ctxt.weirdStringException(p.string, Long::class.java, "Expected number or ISO string")
	}
}
