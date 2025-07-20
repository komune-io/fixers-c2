import io.komune.fixers.gradle.dependencies.FixersDependencies
import io.komune.fixers.gradle.dependencies.FixersVersions

plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
}

dependencies {
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))
	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-dsl"))
	api(project(":c2-ssm:ssm-data:ssm-data-dsl"))

	api("org.springframework.boot:spring-boot-starter-test:${FixersVersions.Spring.boot}")
	FixersDependencies.Jvm.Test.cucumber(::api)
}
