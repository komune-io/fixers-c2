plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-dsl"))
	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-sdk"))

	implementation(libs.slf4j.api)

	testImplementation(project(":c2-ssm:ssm-couchdb:ssm-couchdb-bdd"))
}
