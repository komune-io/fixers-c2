plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
	implementation("io.komune.fixers.gradle:dependencies:0.20.0-SNAPSHOT")
}

