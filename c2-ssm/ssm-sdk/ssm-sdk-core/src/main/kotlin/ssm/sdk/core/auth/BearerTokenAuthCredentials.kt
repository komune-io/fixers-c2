package ssm.sdk.core.auth

interface BearerTokenAuthCredentials: AuthCredentials {
    fun getBearerToken()
}
