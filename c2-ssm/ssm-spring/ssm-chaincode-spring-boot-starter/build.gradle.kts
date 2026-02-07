plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
	id("io.komune.fixers.gradle.publish")
	kotlin("plugin.spring")
	kotlin("kapt")
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-f2"))

	Dependencies.f2SpringFunction(::api)

	Dependencies.springBootConfigProcessor(::kapt)

	testImplementation(project(":c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"))
}
