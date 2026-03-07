plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
}

dependencies {
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))
	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-dsl"))
	api(project(":c2-ssm:ssm-data:ssm-data-dsl"))

	api(libs.spring.boot.starter.test)
	api(libs.bundles.cucumber)
}
