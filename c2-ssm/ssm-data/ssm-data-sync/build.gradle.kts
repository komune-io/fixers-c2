plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-data:ssm-data-dsl"))

	implementation(project(":c2-ssm:ssm-couchdb:ssm-couchdb-f2"))
	implementation(project(":c2-ssm:ssm-data:ssm-data-f2"))

	testImplementation(project(":c2-ssm:ssm-data:ssm-data-bdd"))
}
