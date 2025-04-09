plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
	mavenLocal()
}

dependencies {
	implementation("io.komune.fixers.gradle:dependencies:0.24.0-SNAPSHOT")
}

