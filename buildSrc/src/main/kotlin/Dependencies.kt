import io.komune.gradle.dependencies.FixersDependencies
import io.komune.gradle.dependencies.FixersPluginVersions
import io.komune.gradle.dependencies.FixersVersions
import io.komune.gradle.dependencies.Scope
import io.komune.gradle.dependencies.add
import java.net.URI
import org.gradle.api.artifacts.dsl.RepositoryHandler

object PluginVersions {
	val fixers = FixersPluginVersions.fixers
	val d2 = FixersPluginVersions.fixers
	const val kotlin = FixersPluginVersions.kotlin
	const val springBoot = FixersPluginVersions.springBoot
	const val graalvm = FixersPluginVersions.graalvm
	const val npmPublish = FixersPluginVersions.npmPublish
}

object Versions {

	val f2 = PluginVersions.fixers

	const val slf4j = FixersVersions.Logging.slf4j
	const val jackson = FixersVersions.Json.jackson
	const val jacksonKotlin = FixersVersions.Json.jacksonKotlin

	const val springBoot = FixersVersions.Spring.boot
	const val reactor = FixersVersions.Spring.reactor

	const val ktor = FixersVersions.Kotlin.ktor

	const val fabric = "2.2.26"
	const val fabricGateway = "1.7.1"

	const val cloudant = "0.3.1"
	const val bouncycastleVersion = "1.70"

	const val junit = FixersVersions.Test.junit
	const val assertj = FixersVersions.Test.assertj
}

fun RepositoryHandler.defaultRepo() {
	mavenCentral()
	maven { url = URI("https://s01.oss.sonatype.org/content/repositories/snapshots") }
	maven { url = URI("https://repo.spring.io/milestone") }
	mavenLocal()
}

object Dependencies {
	fun slf4j(scope: Scope) = FixersDependencies.Jvm.Logging.slf4j(scope)

	// TODO Migrate to f2-client
	fun ktor(scope: Scope) = scope.add(
		"io.ktor:ktor-client-core:${Versions.ktor}",
		"io.ktor:ktor-client-content-negotiation:${Versions.ktor}",
		"io.ktor:ktor-client-logging:${Versions.ktor}",
		"io.ktor:ktor-client-cio:${Versions.ktor}",
		"io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}",
		"io.ktor:ktor-serialization-jackson:${Versions.ktor}"
	)

	fun jackson(scope: Scope) = scope.add(
		"com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonKotlin}"
	)

	fun f2Function(scope: Scope) = scope.add(
		"io.komune.f2:f2-spring-boot-starter-function:${Versions.f2}"
	)
	fun f2Http(scope: Scope) = scope.add(
		"io.komune.f2:f2-spring-boot-starter-function-http:${Versions.f2}"
	)
	fun f2Auth(scope: Scope) = scope.add(
		"io.komune.f2:f2-spring-boot-starter-auth-tenant:${Versions.f2}"
	)

	fun springBootConfigurationProcessor(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-configuration-processor:${Versions.springBoot}"
	)

	fun springBootAuthConfiguration(scope: Scope, ksp: Scope)
		= FixersDependencies.Jvm.Spring.autoConfigure(scope, ksp)

	fun springWebFlux(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}",
		"org.springframework.boot:spring-boot-autoconfigure:${Versions.springBoot}",
		"org.springframework.boot:spring-boot-starter-webflux:${Versions.springBoot}"
	)

	fun test(scope: Scope) = scope.add(
		"org.junit.jupiter:junit-jupiter:${Versions.junit}",
		"org.junit.jupiter:junit-jupiter-api:${Versions.junit}",
		"org.assertj:assertj-core:${Versions.assertj}"
	)

	fun fabricSdk(scope: Scope) = scope.add(
		"org.hyperledger.fabric-sdk-java:fabric-sdk-java:${Versions.fabric}"
	)

	fun fabricSdkGateway(scope: Scope) = scope.add(
		"org.hyperledger.fabric:fabric-gateway:${Versions.fabricGateway}"
	)

	fun springTest(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}",
		"io.projectreactor:reactor-test:${Versions.reactor}",
		"org.assertj:assertj-core:${Versions.assertj}"
	)
}
