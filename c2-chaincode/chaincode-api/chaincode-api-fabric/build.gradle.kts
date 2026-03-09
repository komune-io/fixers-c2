plugins {
    alias(libs.plugins.fixers.kotlin.jvm)
}

dependencies {

    implementation(libs.jackson.module.kotlin)
    implementation(libs.slf4j.api)
    api(libs.fabric.sdk.java)

    testImplementation(libs.bundles.test)
}
