plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.spring.boot)
	alias(catalogue.plugins.kotlin.spring)
	alias(catalogue.plugins.ksp)
	alias(catalogue.plugins.kotlin.serialization)
}

dependencies {
	implementation(project(":sample:ssm-sample-orderbook-sourcing-domain"))
	implementation(project(":c2-ssm:ssm-spring:ssm-s2-sourcing-spring-boot-starter"))

	implementation(libs.bundles.spring.redis)
	implementation(catalogue.spring.boot.starter.function.http)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.arrow.core)
	implementation(libs.arrow.optics)
	ksp(libs.arrow.optics.ksp)

	testImplementation(libs.bundles.ssm.sample.testcontainers)
	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.bundles.test)
}
