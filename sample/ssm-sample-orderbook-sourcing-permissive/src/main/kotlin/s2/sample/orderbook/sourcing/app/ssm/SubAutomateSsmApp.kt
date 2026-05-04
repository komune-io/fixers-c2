package s2.sample.orderbook.sourcing.app.ssm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["s2.sample.orderbook.sourcing"])
class SubAutomateSsmApp

fun main(args: Array<String>) {
	runApplication<SubAutomateSsmApp>(*args)
}
