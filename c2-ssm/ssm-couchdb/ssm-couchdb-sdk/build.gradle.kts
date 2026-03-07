plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-couchdb:ssm-couchdb-dsl"))
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-json"))

	api(libs.cloudant)

}
