plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
}

dependencies {
	api(project(":c2-ssm:ssm-chaincode:ssm-chaincode-f2"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-bdd"))
	Dependencies.cucumber(::api)
}
