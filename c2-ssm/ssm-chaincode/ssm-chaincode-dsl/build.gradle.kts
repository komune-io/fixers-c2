plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.mpp)
	alias(catalogue.plugins.fixers.gradle.publish)
	alias(catalogue.plugins.kotlin.serialization)
//	alias(libs.plugins.npm.publish)
}

dependencies {
	commonMainApi(libs.bundles.f2.dsl)
}
