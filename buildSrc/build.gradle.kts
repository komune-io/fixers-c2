plugins {
	`kotlin-dsl`
}

repositories {
	if(System.getenv("MAVEN_LOCAL_USE") == "true") {
		mavenLocal()
	}
	mavenCentral()
	maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
}
val fixersVersion = "0.28.0-SNAPSHOT"
dependencies {
	implementation("io.komune.fixers.gradle:dependencies:$fixersVersion")
}