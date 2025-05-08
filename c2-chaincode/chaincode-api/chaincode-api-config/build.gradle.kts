plugins {
    id("io.komune.fixers.gradle.kotlin.jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
}

dependencies {
    implementation(project(":c2-chaincode:chaincode-dsl"))

    Dependencies.springBootConfigurationProcessor(::kapt)
    Dependencies.f2Function(::implementation)
    Dependencies.jackson(::implementation)
    Dependencies.slf4j(::implementation)
}
