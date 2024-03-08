plugins {
	id("io.komune.fixers.gradle.kotlin.jvm")
	id("io.komune.fixers.gradle.publish")
}

dependencies {
	api(project(":c2-ssm:ssm-tx:ssm-tx-dsl"))

	api(project(":c2-ssm:ssm-sdk:ssm-sdk-sign"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))

}
