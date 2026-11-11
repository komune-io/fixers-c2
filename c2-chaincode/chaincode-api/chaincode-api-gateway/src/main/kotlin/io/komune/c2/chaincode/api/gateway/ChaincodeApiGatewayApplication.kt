package io.komune.c2.chaincode.api.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackageClasses = [ChaincodeApiGatewayApplication::class]
)
@ConfigurationPropertiesScan
class ChaincodeApiGatewayApplication

// The context runs until the JVM exits; Spring takes varargs, so the arguments are copied once at boot.
@Suppress("MissingUseCall", "SpreadOperator")
fun main(args: Array<String>) {
    disableOpenTelemetryExporters()
    val _ = runApplication<ChaincodeApiGatewayApplication>(*args)
}

// The Fabric SDK pulls in the OpenTelemetry OTLP exporters, which keep dialing localhost:4317/4318
// when no collector runs.
private fun disableOpenTelemetryExporters() {
    System.setProperty("otel.traces.exporter", "none")
    System.setProperty("otel.metrics.exporter", "none")
    System.setProperty("otel.logs.exporter", "none")
}
