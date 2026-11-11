plugins {
    alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
    alias(catalogue.plugins.kotlin.serialization)
}

dependencies {
    api(libs.s2.automate.dsl)
    api(catalogue.dsl.function)

    testImplementation(libs.bundles.test)
}
