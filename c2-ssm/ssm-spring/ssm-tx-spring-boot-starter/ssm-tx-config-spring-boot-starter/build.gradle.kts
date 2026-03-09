plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.kotlin.kapt)
}

dependencies {
	api(project(":c2-ssm:ssm-tx:ssm-tx-f2"))

	api(libs.f2.spring.starter.function)

	kapt(libs.spring.boot.configuration.processor)

	testImplementation(project(":c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"))
}
