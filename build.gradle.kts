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
		projectKey = "komune-io_fixers-c2"
		properties {
			// Samples are standalone demo applications and ssm-bdd is published test
			// infrastructure: exclude them from coverage and duplication analysis.
			property("sonar.coverage.exclusions", "**/sample/**,**/ssm-bdd/**")
			property("sonar.cpd.exclusions", "**/sample/**")
			// MPP modules report as jacocoJvmTestReport.xml, JVM modules as
			// jacocoTestReport.xml: match both so coverage reaches SonarCloud.
			property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/**/*.xml")
		}
	}
	repositories {
		sonatypeSnapshots = true
	}
}

subprojects {
	tasks.withType<Test>().configureEach {
		testLogging {
			exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		}
	}
}
