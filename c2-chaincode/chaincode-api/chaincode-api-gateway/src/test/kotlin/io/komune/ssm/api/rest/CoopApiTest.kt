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
			.build().toUri()

		val res = this.restTemplate.getForEntity(uri, String::class.java)
		assertThat(res.statusCode.value()).isEqualTo(200)
		assertThat(res.body).isNotNull
	}
}
