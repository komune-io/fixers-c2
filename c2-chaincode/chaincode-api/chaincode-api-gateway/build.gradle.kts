plugins {
	id("io.spring.dependency-management")
	id("io.komune.fixers.gradle.kotlin.jvm")
	kotlin("plugin.spring")
	id("org.springframework.boot")
}

dependencies {
	implementation(project(":c2-chaincode:chaincode-api:chaincode-api-fabric"))

	Dependencies.springWebFlux(::implementation)
	Dependencies.f2Auth(::implementation)
	Dependencies.jackson(::implementation)

	Dependencies.springTest(::testImplementation)
}
