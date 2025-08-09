import io.komune.fixers.gradle.dependencies.FixersDependencies
import io.komune.fixers.gradle.dependencies.FixersPluginVersions
import io.komune.fixers.gradle.dependencies.FixersVersions
import io.komune.fixers.gradle.dependencies.Scope
import io.komune.fixers.gradle.dependencies.add
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
	const val springSecurity = FixersVersions.Spring.security
	const val reactor = FixersVersions.Spring.reactor

	const val ktor = FixersVersions.Kotlin.ktor

	const val fabric = "2.2.26"

	const val cloudant = "0.3.1"
	const val bouncycastleVersion = "1.70"

	const val junit = FixersVersions.Test.junit
	const val assertj = FixersVersions.Test.assertj
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
		"com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2"
	)

	fun f2Auth(scope: Scope) = scope.add(
		"io.komune.f2:f2-spring-boot-starter-auth-tenant:${Versions.f2}"
	)

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

	fun cucumber(scope: Scope) = FixersDependencies.Jvm.Test.cucumber(scope)

	fun fabricSdk(scope: Scope) = scope.add(
		"org.hyperledger.fabric-sdk-java:fabric-sdk-java:${Versions.fabric}"
	)

	fun springTest(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}",
		"io.projectreactor:reactor-test:${Versions.reactor}",
		"org.assertj:assertj-core:${Versions.assertj}"
	)
}


fun RepositoryHandler.defaultRepo() {
    mavenCentral()
    maven { url = URI("https://central.sonatype.com/repository/maven-snapshots") }
    if(System.getenv("MAVEN_LOCAL_USE") == "true") {
        mavenLocal()
    }
}

