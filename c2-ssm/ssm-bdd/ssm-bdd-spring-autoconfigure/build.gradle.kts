plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
}

dependencies {
	api(project(":c2-ssm:ssm-bdd:ssm-bdd-config"))
}
