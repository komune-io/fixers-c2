plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
	alias(catalogue.plugins.kotlin.spring)
	alias(catalogue.plugins.kotlin.kapt)
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-f2"))

	api(libs.f2.spring.starter.function)

	kapt(libs.spring.boot.configuration.processor)

	testImplementation(project(":c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"))
}
