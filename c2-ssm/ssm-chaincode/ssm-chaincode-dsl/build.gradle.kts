plugins {
	alias(libs.plugins.fixers.kotlin.mpp)
	alias(libs.plugins.fixers.publish)
	alias(libs.plugins.kotlin.serialization)
//	alias(libs.plugins.npm.publish)
}

dependencies {
	commonMainApi(libs.bundles.f2.dsl)
}
