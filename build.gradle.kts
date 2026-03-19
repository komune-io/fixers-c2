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


allprojects {
	tasks.withType<Test> {
		useJUnitPlatform()
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
