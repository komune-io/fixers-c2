plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-data:ssm-data-dsl"))

	implementation(project(":c2-ssm:ssm-couchdb:ssm-couchdb-f2"))
	implementation(project(":c2-ssm:ssm-data:ssm-data-f2"))

	testImplementation(project(":c2-ssm:ssm-data:ssm-data-bdd"))
}
