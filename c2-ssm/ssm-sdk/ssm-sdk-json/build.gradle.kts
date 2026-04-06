plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
}

dependencies {
	implementation(libs.jackson.module.kotlin)
	testImplementation(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
}
