package io.komune.ssm.api.rest

import io.komune.c2.chaincode.api.gateway.ChaincodeApiGatewayApplication
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.web.util.UriComponentsBuilder

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ChaincodeApiGatewayApplication::class]
)
class WebBaseTest {

    @LocalServerPort
    internal var port: Int = 0

    // Built by hand: the app is reactive, and Boot's RestTestClient auto-configuration needs a servlet context.
    internal val restTestClient: RestTestClient by lazy { RestTestClient.bindToServer().build() }

    internal fun baseUrl(): UriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost:$port")
}
