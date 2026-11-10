package ssm.sdk.core.invoke.query

enum class SsmQueryName(val value: String) {
    ADMIN("admin"),
    USER("user"),
    BLOCK("block"),
    LOG("log"),
    SESSION("session"),
    SSM("ssm"),
    TRANSACTION("transaction")
}
