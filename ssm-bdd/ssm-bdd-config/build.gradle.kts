import io.komune.gradle.dependencies.FixersDependencies
import io.komune.gradle.dependencies.FixersVersions

plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
}

dependencies {
	api(project(":ssm-sdk:ssm-sdk-core"))
	api(project(":ssm-couchdb:ssm-couchdb-dsl"))
	api(project(":ssm-data:ssm-data-dsl"))

	api("org.springframework.boot:spring-boot-starter-test:${FixersVersions.Spring.boot}")
	FixersDependencies.Jvm.Test.cucumber(::api)
}
