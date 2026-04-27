plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
}

dependencies {
	implementation(project(":c2-ssm:ssm-couchdb:ssm-couchdb-f2"))
	api(project(":c2-ssm:ssm-bdd:ssm-bdd-features"))

	testImplementation(project(":c2-ssm:ssm-tx:ssm-tx-bdd"))
}
