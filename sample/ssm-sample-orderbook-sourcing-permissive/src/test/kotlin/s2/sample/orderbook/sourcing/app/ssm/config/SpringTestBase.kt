package s2.sample.orderbook.sourcing.app.ssm.config

import com.redis.testcontainers.RedisStackContainer
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import s2.sample.orderbook.sourcing.app.ssm.SubAutomateSsmApp

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [SubAutomateSsmApp::class])
@Suppress("UtilityClassWithPublicConstructor")
abstract class SpringTestBase {

    companion object {
        @Container
        @ServiceConnection
        val redisContainer = RedisStackContainer("redis/redis-stack-server")
    }
}
