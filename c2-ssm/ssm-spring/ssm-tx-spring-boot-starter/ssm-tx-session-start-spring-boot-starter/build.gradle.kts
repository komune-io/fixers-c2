plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
	id("io.komune.fixers.gradle.publish")
	kotlin("plugin.spring")
	kotlin("kapt")
}

dependencies {
	api(project(":c2-ssm:ssm-tx:ssm-tx-f2"))
	api(project(":c2-ssm:ssm-spring:ssm-tx-spring-boot-starter:ssm-tx-config-spring-boot-starter"))
	api("io.komune.f2:f2-spring-boot-starter-function:${Versions.f2}")
	kapt("org.springframework.boot:spring-boot-configuration-processor:${Versions.springBoot}")

	testImplementation(project(":c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"))
}
