package s2.sample.orderbook.storing.app.ssm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["s2.sample.orderbook.storing"])
class OrderBookStoringSsmApp

// The context lives until the JVM exits, and Spring's launcher only takes varargs.
@Suppress("MissingUseCall", "SpreadOperator")
fun main(args: Array<String>) {
    runApplication<OrderBookStoringSsmApp>(*args)
}
