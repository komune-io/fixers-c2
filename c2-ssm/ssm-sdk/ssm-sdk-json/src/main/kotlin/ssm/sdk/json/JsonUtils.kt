package ssm.sdk.json

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.io.IOException
import java.io.Reader

object JsonUtils {

	val mapper: ObjectMapper = ObjectMapper()
//		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//		.setSerializationInclusion(JsonInclude.Include.NON_NULL)
//		.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
//		.registerModule(KotlinModule.Builder().build())
//		.registerModule(JavaTimeModule())

//	@Throws(JsonProcessingException::class)
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
}
