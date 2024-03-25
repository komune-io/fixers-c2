VERSION = $(shell cat VERSION)
DOCKER_REPOSITORY = ghcr.io/

CHAINCODE_APP_NAME	   	 	:= ghcr.io/komune-io/chaincode-api-gateway
CHAINCODE_APP_IMG	    	:= ${CHAINCODE_APP_NAME}:${VERSION}
CHAINCODE_APP_PACKAGE	   	:= :c2-chaincode:chaincode-api:chaincode-api-gateway:bootBuildImage

lint: lint-libs
build: build-libs
test-pre:
	@make dev up
	@make dev c2-sandbox-ssm logs
	@make dev up
	sudo echo "127.0.0.1 ca.bc-coop.bclan" | sudo tee -a /etc/hosts
	sudo echo "127.0.0.1 peer0.bc-coop.bclan" | sudo tee -a /etc/hosts
	sudo echo "127.0.0.1 orderer.bclan" | sudo tee -a /etc/hosts
test: test-libs
test-post:
	@make dev down

publish: publish-libs
promote: promote-libs

# Old task
libs: package-kotlin
package-kotlin: lint-libs build-libs test-libs publish-libs

lint-libs:
	./gradlew detekt

build-libs:
	VERSION=$(VERSION) ./gradlew clean build publishToMavenLocal --refresh-dependencies -x test

test-libs:
	./gradlew test

publish-libs:
	VERSION=$(VERSION) PKG_MAVEN_REPO=github ./gradlew publish --info

promote-libs:
	VERSION=$(VERSION) PKG_MAVEN_REPO=sonatype_oss ./gradlew publish

.PHONY: version
version:
	@echo "$(VERSION)"

chaincode-api-gateway-package: docker-chaincode-api-gateway-build docker-chaincode-api-gateway-push


docker-chaincode-api-gateway-build:
	VERSION=${VERSION} ./gradlew build publishToMavenLocal ${CHAINCODE_APP_PACKAGE} --refresh-dependencies -x test

docker-chaincode-api-gateway-push:
	@docker push ${CHAINCODE_APP_IMG}


## Chaincode
chaincode: chaincode-api-gateway-package
	make package -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-chaincode

## Sandbox
sandbox:
	make package -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox

## ssm
ssm: package-kotlin

## DEV ENVIRONMENT
include infra/docker-compose/dev-compose.mk
