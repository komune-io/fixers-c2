plugins {
	alias(libs.plugins.fixers.kotlin.mpp)
	alias(libs.plugins.fixers.publish)
	alias(libs.plugins.kotlin.serialization)
}

dependencies {
	commonMainApi(project(":c2-ssm:ssm-couchdb:ssm-couchdb-dsl"))
	commonMainApi(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
}
