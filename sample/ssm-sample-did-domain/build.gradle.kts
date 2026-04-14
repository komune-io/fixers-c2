plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.mpp)
	alias(catalogue.plugins.kotlin.serialization)
}

dependencies {
	commonMainImplementation(libs.s2.automate.dsl)
	commonMainApi(catalogue.client.core)
	commonMainApi(catalogue.client.ktor)
	commonMainApi(catalogue.dsl.function)
}
