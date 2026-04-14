plugins {
	alias(catalogue.plugins.kotlin.kapt) apply false
	alias(catalogue.plugins.kotlin.spring) apply false
	alias(catalogue.plugins.kotlin.serialization) apply false
	alias(catalogue.plugins.spring.boot) apply false

	alias(catalogue.plugins.fixers.gradle.kotlin.jvm) apply false
	alias(catalogue.plugins.fixers.gradle.kotlin.mpp) apply false

	alias(catalogue.plugins.f2.bom)
	alias(catalogue.plugins.fixers.gradle.config)
	alias(catalogue.plugins.fixers.gradle.check)
	alias(catalogue.plugins.fixers.gradle.publish)
}


fixers {
	bundle {
		id = "fixers-c2"
		group = "io.komune.c2"
		name = "Chaincode Api and signed state machine"
		description = "Aggregate all ssm data source to optimize request"
		url = "https://github.com/komune-io/fixers-c2"
	}
	sonar {
		organization = "komune-io"
		projectKey = "komune-io_connect-c2"
	}
	repositories {
		sonatypeSnapshots = true
	}
}
