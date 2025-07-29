plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
	id("io.komune.fixers.gradle.publish")
	kotlin("plugin.spring")
	kotlin("kapt")
}

dependencies {
	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-f2"))

	api("io.komune.f2:f2-spring-boot-starter-function:${Versions.f2}")

	kapt("org.springframework.boot:spring-boot-configuration-processor:${Versions.springBoot}")

	testImplementation(project(":c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"))
}
