plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
	id("io.komune.fixers.gradle.publish")
}

dependencies {
	Dependencies.jackson(::implementation)
	testImplementation(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
}
