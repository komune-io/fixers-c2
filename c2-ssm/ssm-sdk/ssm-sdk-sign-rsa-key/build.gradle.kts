plugins {
	alias(libs.plugins.fixers.kotlin.jvm)
	alias(libs.plugins.fixers.publish)
}

dependencies {
	api(project(":c2-ssm:ssm-sdk:ssm-sdk-sign"))
	implementation(libs.bouncycastle)
}
