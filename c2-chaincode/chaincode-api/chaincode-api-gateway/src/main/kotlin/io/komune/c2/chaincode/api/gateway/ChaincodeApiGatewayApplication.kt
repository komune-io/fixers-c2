package io.komune.c2.chaincode.api.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [ChaincodeApiGatewayApplication::class] )
class ChaincodeApiGatewayApplication

fun main(args: Array<String>) {
	disableOpenTelemetryExporters()
	runApplication<ChaincodeApiGatewayApplication>(*args)
}

/**
 * Disables OpenTelemetry OTLP exporters to prevent connection errors.
 *
 * The Hyperledger Fabric SDK (org.hyperledger.fabric-sdk-java:fabric-sdk-java)
 * pulls in OpenTelemetry dependencies:
 * - io.opentelemetry:opentelemetry-sdk
 * - io.opentelemetry:opentelemetry-exporter-otlp
 *
 * By default, these exporters try to connect to localhost:4317 (gRPC) and
 * localhost:4318 (HTTP) to send traces/metrics/logs. When no collector is
 * running, this causes connection errors and performance degradation.
 *
 * Setting these system properties disables the exporters entirely.
 */
private fun disableOpenTelemetryExporters() {
	System.setProperty("otel.traces.exporter", "none")
	System.setProperty("otel.metrics.exporter", "none")
	System.setProperty("otel.logs.exporter", "none")
}
