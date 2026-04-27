plugins {
	alias(catalogue.plugins.kotlin.spring)
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
}

dependencies {
	api(project(":sample:ssm-sample-did-domain"))
	api(project(":c2-ssm:ssm-spring:ssm-s2-storing-spring-boot-starter"))

	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.bundles.test)
}
