plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
	alias(catalogue.plugins.kotlin.spring)
	alias(catalogue.plugins.kotlin.kapt)
}

dependencies {
	api(project(":c2-ssm:ssm-s2:ssm-s2-dsl"))
	api(project(":c2-ssm:ssm-spring:ssm-chaincode-spring-boot-starter"))
	api(project(":c2-ssm:ssm-spring:ssm-tx-spring-boot-starter"))
	api(project(":c2-ssm:ssm-spring:ssm-tx-spring-boot-starter:ssm-tx-init-ssm-spring-boot-starter"))

	api(libs.s2.automate.core)
	api(libs.s2.spring.boot.starter.storing)

	implementation(libs.spring.boot.autoconfigure)
	kapt(libs.spring.boot.configuration.processor)
}
