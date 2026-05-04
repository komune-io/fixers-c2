plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.mpp)
	alias(catalogue.plugins.fixers.gradle.publish)
}

dependencies {
	commonMainApi(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
}
