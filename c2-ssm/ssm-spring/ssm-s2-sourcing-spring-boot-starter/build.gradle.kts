plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
	alias(catalogue.plugins.kotlin.kapt)
	alias(catalogue.plugins.kotlin.serialization)
}

dependencies {
	api(project(":c2-ssm:ssm-s2:ssm-s2-dsl"))
	api(project(":c2-ssm:ssm-spring:ssm-chaincode-spring-boot-starter"))
	api(project(":c2-ssm:ssm-spring:ssm-data-spring-boot-starter"))
	api(project(":c2-ssm:ssm-spring:ssm-tx-spring-boot-starter"))

	api(libs.s2.spring.boot.starter.sourcing)

	implementation(libs.spring.boot.autoconfigure)
	kapt(libs.spring.boot.configuration.processor)

	testImplementation(libs.bundles.test)
}
