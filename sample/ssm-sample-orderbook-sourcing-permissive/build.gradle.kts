plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.spring.boot)
	alias(catalogue.plugins.kotlin.spring)
}

dependencies {
	implementation(project(":sample:ssm-sample-orderbook-sourcing-common"))

	implementation(catalogue.spring.boot.starter.function.http)
	implementation(libs.kotlinx.serialization.json)

	testImplementation(testFixtures(project(":sample:ssm-sample-orderbook-sourcing-common")))
	testImplementation(libs.bundles.ssm.sample.testcontainers)
	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.bundles.test)
}

springBoot {
	mainClass.set("s2.sample.orderbook.sourcing.app.ssm.SubAutomateSsmAppKt")
}
