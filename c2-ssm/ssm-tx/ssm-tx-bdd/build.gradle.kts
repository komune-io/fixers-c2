plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
}

dependencies {
	implementation(project(":c2-ssm:ssm-tx:ssm-tx-f2"))

	implementation(project(":c2-ssm:ssm-sdk:ssm-sdk-bdd"))
	implementation(project(":c2-ssm:ssm-chaincode:ssm-chaincode-bdd"))

	api(libs.bundles.cucumber)
}
