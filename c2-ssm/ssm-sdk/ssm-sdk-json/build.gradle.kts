plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	implementation(libs.jackson.module.kotlin)
	testImplementation(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
}
