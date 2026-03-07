plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-data:ssm-data-dsl"))

	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-f2"))
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-f2"))

	testImplementation(project(":c2-ssm:ssm-data:ssm-data-bdd"))
}
