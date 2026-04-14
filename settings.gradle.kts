pluginManagement {
	repositories {
		if(System.getenv("FIXERS_REPOSITORIES_MAVEN_LOCAL") == "true") {
			mavenLocal()
		}
		gradlePluginPortal()
		mavenCentral()
		maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
	}
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
		if(System.getenv("FIXERS_REPOSITORIES_MAVEN_LOCAL") == "true") {
			mavenLocal()
		}
	}
	versionCatalogs {
		val fixersVersion = file("gradle/libs.versions.toml")
			.readLines()
			.firstNotNullOfOrNull {
				Regex("^fixers\\s*=\\s*\"([^\"]+)\"").find(it)?.groupValues?.get(1)
			} ?: error("fixers version not found in gradle/libs.versions.toml")
		create("catalogue") {
			from("io.komune.f2:f2-gradle-catalog:$fixersVersion")
		}
	}
}

include(
	"c2-ssm:ssm-bdd:ssm-bdd-config",
	"c2-ssm:ssm-bdd:ssm-bdd-features",
	"c2-ssm:ssm-bdd:ssm-bdd-spring-autoconfigure"
)

include(
	"c2-ssm:ssm-chaincode:ssm-chaincode-bdd",
	"c2-ssm:ssm-chaincode:ssm-chaincode-dsl",
	"c2-ssm:ssm-chaincode:ssm-chaincode-f2",
	"c2-ssm:ssm-chaincode:ssm-chaincode-f2-client",
)

include(
	"c2-ssm:ssm-couchdb:ssm-couchdb-bdd",
	"c2-ssm:ssm-couchdb:ssm-couchdb-sdk",
	"c2-ssm:ssm-couchdb:ssm-couchdb-dsl",
	"c2-ssm:ssm-couchdb:ssm-couchdb-f2",
)

include(
	"c2-ssm:ssm-data:ssm-data-bdd",
	"c2-ssm:ssm-data:ssm-data-dsl",
	"c2-ssm:ssm-data:ssm-data-f2",
	"c2-ssm:ssm-data:ssm-data-sync",
)

include(
	"c2-ssm:ssm-sdk:ssm-sdk-bdd",
	"c2-ssm:ssm-sdk:ssm-sdk-dsl",
	"c2-ssm:ssm-sdk:ssm-sdk-core",
	"c2-ssm:ssm-sdk:ssm-sdk-json",
	"c2-ssm:ssm-sdk:ssm-sdk-sign",
	"c2-ssm:ssm-sdk:ssm-sdk-sign-rsa-key",
)

include(
	"c2-ssm:ssm-spring:ssm-chaincode-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-couchdb-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-data-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-tx-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-tx-spring-boot-starter:ssm-tx-config-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-tx-spring-boot-starter:ssm-tx-create-ssm-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-tx-spring-boot-starter:ssm-tx-init-ssm-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-tx-spring-boot-starter:ssm-tx-session-perform-action-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-tx-spring-boot-starter:ssm-tx-session-start-spring-boot-starter"
)

include(
	"c2-ssm:ssm-tx:ssm-tx-bdd",
	"c2-ssm:ssm-tx:ssm-tx-dsl",
	"c2-ssm:ssm-tx:ssm-tx-f2",
)

include(
	"c2-ssm:ssm-s2:ssm-s2-dsl",
)

include(
	"c2-ssm:ssm-spring:ssm-s2-storing-spring-boot-starter",
	"c2-ssm:ssm-spring:ssm-s2-sourcing-spring-boot-starter",
)

include(
	"c2-chaincode:chaincode-api:chaincode-api-fabric",
	"c2-chaincode:chaincode-api:chaincode-api-gateway",
)

include(
	"sample:ssm-sample-orderbook-sourcing-domain",
	"sample:ssm-sample-orderbook-sourcing",
	"sample:ssm-sample-orderbook-sourcing-permissive",
	"sample:ssm-sample-orderbook-storing",
	"sample:ssm-sample-did-domain",
	"sample:ssm-sample-did-app",
)
