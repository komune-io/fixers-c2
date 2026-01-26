plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
}

dependencies {
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))
	api(project(":c2-ssm:ssm-bdd:ssm-bdd-features"))
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-dsl"))

	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))
	Dependencies.jackson(::implementation)
	Dependencies.cucumber(::api)
}
