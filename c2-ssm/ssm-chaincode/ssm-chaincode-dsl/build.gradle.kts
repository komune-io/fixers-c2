plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.mpp)
	alias(catalogue.plugins.fixers.gradle.publish)
	alias(catalogue.plugins.kotlin.serialization)
}

dependencies {
	commonMainApi(project(":c2-chaincode:chaincode-dsl"))
	commonMainApi(libs.bundles.f2.dsl)
}
