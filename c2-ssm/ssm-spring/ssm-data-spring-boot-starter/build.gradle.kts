plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
	id("io.komune.fixers.gradle.publish")
	kotlin("plugin.spring")
	kotlin("kapt")
}

dependencies {
	api(project(":c2-ssm:ssm-data:ssm-data-f2"))
	api(project(":c2-ssm:ssm-data:ssm-data-sync"))

	Dependencies.f2SpringFunction(::api)
	Dependencies.springBootConfigProcessor(::kapt)

	Dependencies.slf4j(::implementation)

	testImplementation(project(":c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"))
}