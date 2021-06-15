package ssm.f2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages= ["ssm.f2"])
class TestApplication

fun main(args: Array<String>) {
	runApplication<TestApplication>(*args)
}