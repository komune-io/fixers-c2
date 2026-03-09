plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))

	testImplementation(project(":c2-ssm:ssm-chaincode:ssm-chaincode-bdd"))
	testImplementation(project(":c2-ssm:ssm-tx:ssm-tx-bdd"))
}
