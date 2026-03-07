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

	const val springBoot = FixersVersions.Spring.boot
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
		"io.ktor:ktor-client-core",
		"io.ktor:ktor-client-content-negotiation",
		"io.ktor:ktor-client-logging",
		"io.ktor:ktor-client-cio",
		"io.ktor:ktor-serialization-kotlinx-json",
		"io.ktor:ktor-serialization-jackson"
	)

	fun jackson(scope: Scope) = FixersDependencies.Jvm.Json.jackson(scope)

	fun f2Client(scope: Scope) = scope.add(
		"io.komune.f2:f2-client-ktor",
		"io.komune.f2:f2-dsl-cqrs",
		"io.komune.f2:f2-dsl-function"
	)

	fun f2Dsl(scope: Scope) = scope.add(
		"io.komune.f2:f2-dsl-cqrs",
		"io.komune.f2:f2-dsl-function"
	)

	fun f2SpringFunction(scope: Scope) = scope.add(
		"io.komune.f2:f2-spring-boot-starter-function"
	)

	fun f2SpringFunctionHttp(scope: Scope) = scope.add(
		"io.komune.f2:f2-spring-boot-starter-function-http"
	)

	fun f2Auth(scope: Scope) = scope.add(
		"io.komune.f2:f2-spring-boot-starter-auth-tenant"
	)

	fun cloudant(scope: Scope) = scope.add(
		"com.ibm.cloud:cloudant:${Versions.cloudant}"
	)

	fun springWebFlux(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-starter-actuator",
		"org.springframework.boot:spring-boot-autoconfigure",
		"org.springframework.boot:spring-boot-starter-webflux"
	)

	fun springBootConfigProcessor(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-configuration-processor"
	)

	fun springBootStarterTest(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-starter-test"
	)

	fun test(scope: Scope) = scope.add(
		"org.junit.jupiter:junit-jupiter",
		"org.junit.jupiter:junit-jupiter-api",
		"org.assertj:assertj-core"
	)

	fun cucumber(scope: Scope) = FixersDependencies.Jvm.Test.cucumber(scope)

	fun fabricSdk(scope: Scope) = scope.add(
		"org.hyperledger.fabric-sdk-java:fabric-sdk-java:${Versions.fabric}"
	)

	fun springTest(scope: Scope) = scope.add(
		"org.springframework.boot:spring-boot-starter-test",
		"org.springframework.boot:spring-boot-restclient",
		"org.springframework.boot:spring-boot-resttestclient",
		"io.projectreactor:reactor-test",
		"org.assertj:assertj-core"
	)

	fun bouncyCastle(scope: Scope) = scope.add(
		"org.bouncycastle:bcprov-jdk15on:${Versions.bouncycastleVersion}"
	)
}


fun RepositoryHandler.defaultRepo() {
    mavenCentral()
    maven { url = URI("https://central.sonatype.com/repository/maven-snapshots") }
    if(System.getenv("MAVEN_LOCAL_USE") == "true") {
        mavenLocal()
    }
}

