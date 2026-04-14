plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.mpp)
	alias(catalogue.plugins.ksp)
	alias(catalogue.plugins.kotlin.serialization)
}

// Workaround for Kotlin/JS production compiler crash with KSP-generated arrow-optics code
tasks.matching { it.name == "compileProductionLibraryKotlinJs" }.configureEach {
	enabled = false
}

dependencies {
	commonMainApi(libs.s2.automate.dsl)
	commonMainApi(catalogue.dsl.function)
	commonMainApi(libs.arrow.core)
	commonMainApi(libs.arrow.optics)
	kspJvm(libs.arrow.optics.ksp)
}
