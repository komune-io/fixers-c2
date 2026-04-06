plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))

	testImplementation(project(":c2-ssm:ssm-chaincode:ssm-chaincode-bdd"))
	testImplementation(project(":c2-ssm:ssm-tx:ssm-tx-bdd"))
}
