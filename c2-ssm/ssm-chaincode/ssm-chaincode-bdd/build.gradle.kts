plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-f2"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-bdd"))
	api(libs.bundles.cucumber)
}
