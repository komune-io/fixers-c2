plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":ssm-sdk:ssm-sdk-sign"))
    implementation(project(":ssm-chaincode:ssm-chaincode-dsl"))
    implementation(project(":ssm-sdk:ssm-sdk-json"))
    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")



    implementation("org.bouncycastle:bcprov-jdk15on:${ Versions.bouncycastleVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${ Versions.jacksonKotlin}")

    implementation("com.squareup.okhttp3:okhttp:${Versions.okhttpVersion}")
    implementation("com.squareup.retrofit2:retrofit:${ Versions.retrofitVersion}")
    implementation("com.squareup.retrofit2:converter-jackson:${Versions.retrofitVersion}")

}

apply(from = rootProject.file("gradle/publishing.gradle"))