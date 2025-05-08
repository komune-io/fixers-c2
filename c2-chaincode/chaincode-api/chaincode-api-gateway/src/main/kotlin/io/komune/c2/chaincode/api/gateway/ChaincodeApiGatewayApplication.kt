package io.komune.c2.chaincode.api.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication(
	scanBasePackageClasses = [ChaincodeApiGatewayApplication::class]
)
class ChaincodeApiGatewayApplication

fun main(args: Array<String>) {
	runApplication<ChaincodeApiGatewayApplication>(*args)
}
