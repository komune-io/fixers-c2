plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.kotlin.spring)
}

dependencies {
	api(project(":c2-ssm:ssm-bdd:ssm-bdd-config"))
}
