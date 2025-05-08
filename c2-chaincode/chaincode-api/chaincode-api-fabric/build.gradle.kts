plugins {
    id("io.komune.fixers.gradle.kotlin.jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
}

dependencies {
    implementation(project(":c2-chaincode:chaincode-api:chaincode-api-config"))
    implementation(project(":c2-chaincode:chaincode-dsl"))

    Dependencies.springBootAuthConfiguration(::implementation, ::kapt)
    Dependencies.jackson(::implementation)
    Dependencies.slf4j(::implementation)


    // Enforce platform versions
    implementation(enforcedPlatform("io.grpc:grpc-bom:1.67.1"))
    implementation(enforcedPlatform("com.google.protobuf:protobuf-bom:4.28.2"))

    // Main dependencies
    api("org.hyperledger.fabric:fabric-gateway:1.7.1") {
        exclude(group = "org.hyperledger.fabric", module = "fabric-protos")
    }
    implementation("org.hyperledger.fabric:fabric-protos:0.3.2")

    // Explicitly declare protobuf and grpc dependencies
    implementation("com.google.protobuf:protobuf-java")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")
    implementation("io.grpc:grpc-netty-shaded")

    Dependencies.test(::testImplementation)
}
