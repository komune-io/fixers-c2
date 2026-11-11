plugins {
    alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
    alias(catalogue.plugins.spring.boot)
    alias(catalogue.plugins.kotlin.spring)
    alias(catalogue.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":sample:ssm-sample-orderbook-sourcing-domain"))
    implementation(project(":c2-ssm:ssm-spring:ssm-s2-storing-spring-boot-starter"))

    implementation(catalogue.spring.boot.starter.function.http)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.bundles.test)
}
