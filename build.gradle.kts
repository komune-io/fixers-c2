plugins {
	alias(libs.plugins.kotlin.kapt) apply false
	alias(libs.plugins.kotlin.spring) apply false
	alias(libs.plugins.kotlin.serialization) apply false
	alias(libs.plugins.spring.boot) apply false
	alias(libs.plugins.graalvm) apply false
	alias(libs.plugins.npm.publish) apply false
	alias(libs.plugins.fixers.config)
	alias(libs.plugins.fixers.check)
	alias(libs.plugins.fixers.publish)
	alias(libs.plugins.fixers.kotlin.jvm) apply false
	alias(libs.plugins.fixers.kotlin.mpp) apply false
	alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
	group = "io.komune.c2"
	version = System.getenv("VERSION") ?: "experimental-SNAPSHOT"
	repositories {
		if (System.getenv("MAVEN_LOCAL_USE") == "true") {
			mavenLocal()
		}
		mavenCentral()
		maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
	}
}

subprojects {
	pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
		dependencies {
			"api"(platform(libs.f2.bom))
		}
	}
	pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
		dependencies {
			"commonMainApi"(platform(libs.f2.bom))
		}
	}
	plugins.withType(dev.petuska.npm.publish.NpmPublishPlugin::class.java).whenPluginAdded {
		the<dev.petuska.npm.publish.extension.NpmPublishExtension>().apply {
			organization.set("komune")
			registries {
				register("npmjs") {
					uri.set(uri("https://registry.npmjs.org"))
					authToken.set(System.getenv("NPM_TOKEN"))
				}
			}
		}
	}
}

val aggregatedTests = mutableMapOf<String,String>()
val aggregatedTestResults = mutableMapOf(
	"total" to 0L,
	"passed" to 0L,
	"failed" to 0L,
	"skipped" to 0L
)

allprojects {
	tasks.withType<Test> {
		useJUnitPlatform()

		addTestListener(object : TestListener {
			override fun beforeSuite(suite: TestDescriptor) {}

			override fun afterSuite(suite: TestDescriptor, result: TestResult) {
				if (suite.parent == null) {
					synchronized(aggregatedTestResults) {
						aggregatedTestResults["total"] = aggregatedTestResults["total"]!! + result.testCount
						aggregatedTestResults["passed"] = aggregatedTestResults["passed"]!! + result.successfulTestCount
						aggregatedTestResults["failed"] = aggregatedTestResults["failed"]!! + result.failedTestCount
						aggregatedTestResults["skipped"] = aggregatedTestResults["skipped"]!! + result.skippedTestCount
					}
				}
			}

			override fun beforeTest(testDescriptor: TestDescriptor) {}

			override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
				aggregatedTests["${testDescriptor.className} ${testDescriptor.displayName} ${result.resultType.name}"] = result.resultType.name
			}
		})
		finalizedBy(":aggregateTestResults")
	}
}

val aggregateTestResults by tasks.registering {
	group = "verification"
	description = "Display aggregated test results for all submodules."

	doLast {
		println("""
            ==================================================
            Aggregated Test Results:
            Total: ${aggregatedTestResults["total"]},
            Passed: ${aggregatedTestResults["passed"]},
            Failed: ${aggregatedTestResults["failed"]},
            Skipped: ${aggregatedTestResults["skipped"]}
            ==================================================
        """.trimIndent())
		aggregatedTests.forEach { (test, result) ->
			println("$test: $result")
		}
	}
}

fixers {
//	d2 {
//		outputDirectory = file("storybook/stories/d2/")
//	}
	bundle {
		id = "c2"
		name = "Chaincode Api and signed state machine"
		description = "Aggregate all ssm data source to optimize request"
		url = "https://github.com/komune-io/fixers-c2"
	}
	sonar {
		organization = "komune-io"
		projectKey = "komune-io_connect-c2"
	}
}
