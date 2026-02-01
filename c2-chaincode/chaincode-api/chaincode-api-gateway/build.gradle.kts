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

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
	// Increase direct memory limit for Netty buffers (Paketo default is 10MB)
	// Netty allocates ~31MB direct buffers for gRPC/HTTP connections
	environment.set(mapOf(
		"BPE_APPEND_JAVA_TOOL_OPTIONS" to " -XX:MaxDirectMemorySize=64m"
	))
}
