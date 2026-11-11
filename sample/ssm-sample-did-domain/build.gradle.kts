plugins {
    alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
    alias(catalogue.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.s2.automate.dsl)
    api(catalogue.client.core)
    api(catalogue.client.ktor)
    api(catalogue.dsl.function)

    testImplementation(libs.bundles.test)
}
