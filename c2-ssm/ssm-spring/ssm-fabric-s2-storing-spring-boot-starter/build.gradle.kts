plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
	alias(catalogue.plugins.kotlin.spring)
	alias(catalogue.plugins.kotlin.kapt)
}

dependencies {
	api(project(":c2-ssm:ssm-spring:ssm-s2-storing-spring-boot-starter"))

	api(project(":c2-chaincode:chaincode-api:chaincode-api-fabric"))

	api(project(":c2-chaincode:chaincode-api:chaincode-api-config"))

	implementation(libs.spring.boot.autoconfigure)
	kapt(libs.spring.boot.configuration.processor)

	testImplementation(libs.bundles.test)
	testImplementation(libs.mockk)
}
