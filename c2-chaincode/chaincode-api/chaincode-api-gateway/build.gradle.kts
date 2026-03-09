plugins {
	alias(libs.plugins.spring.dependency.management)
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.spring.boot)
}

dependencies {
	implementation(project(":c2-chaincode:chaincode-api:chaincode-api-fabric"))

	implementation(libs.bundles.spring.webflux)
	implementation(libs.f2.spring.starter.auth.tenant)
	implementation(libs.jackson.module.kotlin)

	testImplementation(libs.bundles.spring.test)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
	// Increase direct memory limit for Netty buffers (Paketo default is 10MB)
	// Netty allocates ~31MB direct buffers for gRPC/HTTP connections
	environment.set(mapOf(
		"BPE_APPEND_JAVA_TOOL_OPTIONS" to " -XX:MaxDirectMemorySize=64m"
	))
}
