plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
}

dependencies {
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-core"))
	api(project(":c2-ssm:ssm-bdd:ssm-bdd-config"))
	api(libs.bundles.test)
}
