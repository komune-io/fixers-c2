plugins {
    alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
    alias(catalogue.plugins.kotlin.spring)
    alias(catalogue.plugins.kotlin.kapt)
}

dependencies {
    implementation(project(":c2-chaincode:chaincode-dsl"))
    kapt(libs.spring.boot.configuration.processor)
    implementation(libs.f2.spring.starter.function)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.slf4j.api)
}
