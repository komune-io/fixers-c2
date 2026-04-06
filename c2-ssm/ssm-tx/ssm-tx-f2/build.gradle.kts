plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-tx:ssm-tx-dsl"))

	api(project(":c2-ssm:ssm-sdk:ssm-sdk-sign"))
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))

}
