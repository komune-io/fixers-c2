plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.kotlin.spring)
	`java-test-fixtures`
}

dependencies {
	api(project(":sample:ssm-sample-orderbook-sourcing-domain"))
	api(project(":c2-ssm:ssm-spring:ssm-s2-sourcing-spring-boot-starter"))

	implementation(libs.bundles.spring.redis)

	testFixturesImplementation(libs.bundles.ssm.sample.testcontainers)
	testFixturesImplementation(libs.spring.boot.starter.test)
}
