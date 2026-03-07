plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-dsl"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-json"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-sign"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-sign-rsa-key"))

	implementation(libs.bundles.ktor)
	implementation(libs.jackson.module.kotlin)

	testImplementation(project(":c2-ssm:ssm-sdk:ssm-sdk-bdd"))
}
