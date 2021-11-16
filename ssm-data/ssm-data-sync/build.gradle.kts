plugins {
	id("city.smartb.fixers.gradle.kotlin.jvm")
	id("city.smartb.fixers.gradle.publish")
}

dependencies {
	api(project(":ssm-data:ssm-data-dsl"))

	implementation(project(":ssm-couchdb:ssm-couchdb-f2"))
	implementation(project(":ssm-data:ssm-data-f2"))

	testImplementation(project(":ssm-data:ssm-data-bdd"))
}
