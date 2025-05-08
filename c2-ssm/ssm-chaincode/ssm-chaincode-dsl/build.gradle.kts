plugins {
	id("io.komune.fixers.gradle.kotlin.mpp")
	id("io.komune.fixers.gradle.publish")
	kotlin("plugin.serialization")
//	id("dev.petuska.npm.publish")
}

dependencies {
	commonMainApi(project(":c2-chaincode:chaincode-dsl"))

	commonMainApi("io.komune.f2:f2-dsl-cqrs:${Versions.f2}")
	commonMainApi("io.komune.f2:f2-dsl-function:${Versions.f2}")
}
