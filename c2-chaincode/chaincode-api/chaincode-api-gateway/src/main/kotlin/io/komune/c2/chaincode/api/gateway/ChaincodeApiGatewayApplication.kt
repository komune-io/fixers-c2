package io.komune.c2.chaincode.api.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication(scanBasePackageClasses = [ChaincodeApiGatewayApplication::class] )
class ChaincodeApiGatewayApplication

fun main(args: Array<String>) {
	System.setProperty("otel.sdk.disabled", "true")
	System.setProperty("org.hyperledger.fabric.sdk.client.thread_executor_corepoolsize", "200")
	runApplication<ChaincodeApiGatewayApplication>(*args)
}
