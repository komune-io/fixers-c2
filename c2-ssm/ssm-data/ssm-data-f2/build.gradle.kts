plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-data:ssm-data-dsl"))

	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-f2"))
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-f2"))

	testImplementation(project(":c2-ssm:ssm-data:ssm-data-bdd"))
}
