plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
	id("io.komune.fixers.gradle.publish")
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-dsl"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-json"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-sign"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-sign-rsa-key"))

//	FixersDependencies.Jvm.Kotlin.ktorClient(::implementation)
//	api("io.ktor:ktor-client-content-negotiation:2.0.0")

	Dependencies.ktor(::implementation)
	Dependencies.jackson(::implementation)

	testImplementation(project(":c2-ssm:ssm-sdk:ssm-sdk-bdd"))
}
