pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		if(System.getenv("MAVEN_LOCAL_USE") == "true") {
			mavenLocal()
		}
	}
}

rootProject.name = "fixers-c2"

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
	"c2-ssm:ssm-data:ssm-data-client",
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

//include(
//	"sample:ssm-full",
//	"sample:ssm-full-ext",
//)

include(
	"c2-ssm:ssm-tx:ssm-tx-bdd",
	"c2-ssm:ssm-tx:ssm-tx-dsl",
	"c2-ssm:ssm-tx:ssm-tx-f2",
)

include(
	"c2-chaincode:chaincode-api:chaincode-api-fabric",
	"c2-chaincode:chaincode-api:chaincode-api-gateway",
)
