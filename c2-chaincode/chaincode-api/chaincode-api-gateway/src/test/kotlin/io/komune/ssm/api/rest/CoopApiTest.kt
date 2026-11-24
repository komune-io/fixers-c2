package io.komune.ssm.api.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CoopApiTest : WebBaseTest() {

    @Test
    fun shouldReturnNotEmptyValue_WhenExecuteQuery() {
        val uri = baseUrl()
            .queryParam("cmd", "query")
            .queryParam("fcn", "query")
            .queryParam("args", "a")
            .build()
            .toUri()

        val body = restTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String::class.java)
            .returnResult()
            .responseBody
        assertThat(body).isNotNull
    }
}
