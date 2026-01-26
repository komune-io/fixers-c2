package ssm.sdk.json

import tools.jackson.core.type.TypeReference

class JSONConverterObjectMapper : JSONConverter {


	override fun <T> toCompletableObjects(clazz: Class<T>, value: String): List<T> {
		val type: TypeReference<List<T>> = object : TypeReference<List<T>>() {}
		return JsonUtils.toObject(value, type)
	}

	override fun <T> toCompletableObject(clazz: Class<T>, value: String): T? {
		return toObject(clazz, value)
	}

	override fun <T> toObject(clazz: Class<T>, value: String): T?  {
		return if (value.isBlank()) {
			null
		} else {
			JsonUtils.toObject(value, clazz)
		}
	}
}
