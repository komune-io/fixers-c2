plugins {
	alias(libs.plugins.fixers.kotlin.mpp)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	commonMainApi(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
}
