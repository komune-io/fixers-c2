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

dependencies {
	implementation("io.komune.fixers.gradle:dependencies:0.24.1")
}

