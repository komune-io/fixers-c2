plugins {
	id("io.spring.dependency-management")
	id("io.komune.fixers.gradle.kotlin.jvm")
	kotlin("plugin.spring")
	id("org.springframework.boot")
}

dependencies {
	implementation(project(":c2-chaincode:chaincode-api:chaincode-api-config"))
	implementation(project(":c2-chaincode:chaincode-dsl"))
	implementation(project(":c2-chaincode:chaincode-api:chaincode-api-fabric"))

	Dependencies.springWebFlux(::implementation)
	Dependencies.f2Auth(::implementation)
	Dependencies.f2Http(::implementation)
	Dependencies.jackson(::implementation)

	Dependencies.springTest(::testImplementation)
}
