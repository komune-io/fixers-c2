package s2.sample.orderbook.sourcing.app.ssm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["s2.sample.orderbook.sourcing"])
class SubAutomateSsmApp

// The context lives until the JVM exits, and Spring's launcher only takes varargs.
@Suppress("MissingUseCall", "SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SubAutomateSsmApp>(*args)
}
