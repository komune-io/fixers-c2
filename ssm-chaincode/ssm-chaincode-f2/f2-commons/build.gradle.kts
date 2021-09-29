plugins {
	id("city.smartb.fixers.gradle.kotlin.jvm")
	id("city.smartb.fixers.gradle.publish")
}

dependencies {
	api(project(":ssm-chaincode:ssm-chaincode-dsl"))

	api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}")
	implementation(project(":ssm-sdk:ssm-sdk-core"))
	implementation(project(":ssm-sdk:ssm-sdk-json"))
}
