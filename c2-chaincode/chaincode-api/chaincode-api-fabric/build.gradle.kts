plugins {
    alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
    alias(catalogue.plugins.fixers.gradle.publish)
    alias(catalogue.plugins.kotlin.spring)
    alias(catalogue.plugins.kotlin.kapt)
}

dependencies {
    implementation(project(":c2-chaincode:chaincode-api:chaincode-api-config"))
    implementation(project(":c2-chaincode:chaincode-dsl"))

    kapt(libs.spring.boot.configuration.processor)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.slf4j.api)

    // Enforce platform versions
    implementation(enforcedPlatform(libs.grpc.bom))
    implementation(enforcedPlatform(libs.protobuf.bom))

    // Fabric Gateway API
    api(libs.fabric.gateway) {
        exclude(group = "org.hyperledger.fabric", module = "fabric-protos")
    }
    implementation(libs.fabric.protos)

    // gRPC/protobuf
    implementation(libs.protobuf.java)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.netty.shaded)

    testImplementation(libs.bundles.test)
}

// enforcedPlatform(grpc/protobuf BOMs) is required: fabric-gateway is compiled against
// specific gRPC/Netty/protobuf ABIs and drifting any of them produces NoSuchMethodError /
// NoClassDefFoundError at runtime. Plain `platform(...)` would let downstream BOMs
// (e.g. Spring Boot) win conflict resolution and break Fabric compat.
// `suppressedValidationErrors.add("enforced-platform")` is an intentional escape hatch:
// Gradle blocks publishing components with enforced platforms by default because the
// forced constraints leak transitively to consumers — accepted trade-off here.
tasks.withType<GenerateModuleMetadata>().configureEach {
    suppressedValidationErrors.add("enforced-platform")
}
