plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.kotlin.kapt)
}

dependencies {
	api(project(":c2-ssm:ssm-data:ssm-data-f2"))
	api(project(":c2-ssm:ssm-data:ssm-data-sync"))

	api(libs.f2.spring.starter.function)
	kapt(libs.spring.boot.configuration.processor)

	implementation(libs.slf4j.api)

	testImplementation(project(":c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"))
}
