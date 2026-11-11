package ssm.sdk.json

interface JSONConverter {
    fun <T> toCompletableObjects(clazz: Class<T>, value: String): List<T>
    fun <T> toCompletableObject(clazz: Class<T>, value: String): T?
    fun <T> toObject(clazz: Class<T>, value: String): T?
}
