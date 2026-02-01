package io.komune.ssm.api.rest

import io.komune.c2.chaincode.api.gateway.ChaincodeApiGatewayApplication
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.util.UriComponentsBuilder


@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ChaincodeApiGatewayApplication::class]
)
@AutoConfigureTestRestTemplate
class WebBaseTest {

    @LocalServerPort
    internal var port: Int = 0

    @Autowired
    internal lateinit var restTemplate: TestRestTemplate

    internal fun baseUrl(): UriComponentsBuilder {
        return UriComponentsBuilder.fromUriString("http://localhost:$port")
    }
}
