plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
	mavenLocal()
}

dependencies {
	implementation("io.komune.fixers.gradle:dependencies:0.24.0-SNAPSHOT")
}

