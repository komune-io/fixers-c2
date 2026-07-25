package ssm.sdk.json

class JSONConverterObjectMapper : JSONConverter {


	override fun <T> toCompletableObjects(clazz: Class<T>, value: String): List<T> {
		if (value.isBlank()) {
			return emptyList()
		}
		return JsonUtils.toObjects(value, clazz)
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
