plugins {
	kotlin("plugin.spring") version PluginVersions.kotlin apply false
	kotlin("plugin.serialization") version PluginVersions.kotlin apply false
	kotlin("kapt") version PluginVersions.kotlin apply false
	id("org.springframework.boot") version PluginVersions.springBoot apply false
	id("org.graalvm.buildtools.native") version PluginVersions.graalvm apply false

	id("dev.petuska.npm.publish") version PluginVersions.npmPublish apply false
	id("com.moowork.node") version "1.2.0"

	id("io.komune.fixers.gradle.config") version PluginVersions.fixers
	id("io.komune.fixers.gradle.check") version PluginVersions.fixers
	id("io.komune.fixers.gradle.d2") version PluginVersions.d2
}

allprojects {
	group = "io.komune.c2"
	version = System.getenv("VERSION") ?: "experimental-SNAPSHOT"
	repositories {
		defaultRepo()
	}
}

subprojects {
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


tasks {

	create<com.moowork.gradle.node.yarn.YarnTask>("installYarn") {
		dependsOn("build")
		args = listOf("install")
	}

	create<com.moowork.gradle.node.yarn.YarnTask>("storybook") {
		dependsOn("yarn_install")
		args = listOf("storybook")
	}
}

fixers {
	d2 {
		outputDirectory = file("storybook/stories/d2/")
	}
//	jdk {
//		version = 21
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
