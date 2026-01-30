plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
	if(System.getenv("MAVEN_LOCAL_USE") == "true") {
		mavenLocal()
	}
}
val fixersVersion = "0.27.0-SNAPSHOT"
dependencies {
	implementation("io.komune.fixers.gradle:dependencies:$fixersVersion")
}