plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
}

dependencies {
	api(project(":c2-ssm:ssm-data:ssm-data-f2"))
	api(project(":c2-ssm:ssm-bdd:ssm-bdd-features"))
	api(project(":c2-ssm:ssm-tx:ssm-tx-bdd"))
}
