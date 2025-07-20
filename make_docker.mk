VERSION = $(shell cat VERSION)

CHAINCODE_APP_NAME	   	 	:= c2-chaincode-api-gateway
CHAINCODE_APP_IMG	    	:= ${CHAINCODE_APP_NAME}:${VERSION}
CHAINCODE_APP_PACKAGE	   	:= :c2-chaincode:chaincode-api:chaincode-api-gateway:bootBuildImage

.PHONY: lint build test stage promote

lint:
	make lint -C c2-chaincode -e VERSION=$(VERSION)
	make lint -C c2-sandbox -e VERSION=$(VERSION)

build: docker-chaincode-api-gateway-build
	make build -C c2-chaincode -e VERSION=$(VERSION)
	make build -C c2-sandbox -e VERSION=$(VERSION)

stage: docker-chaincode-api-gateway-stage
	make push -e DOCKER_REPOSITORY=ghcr.io/komune-io/ -C c2-chaincode -e VERSION=$(VERSION)
	make push -e DOCKER_REPOSITORY=ghcr.io/komune-io/ -C c2-sandbox -e VERSION=$(VERSION)

promote: docker-chaincode-api-gateway-promote
	make promote -e DOCKER_REPOSITORY=docker.io/komune/ -C c2-chaincode -e VERSION=$(VERSION)
	make promote -e DOCKER_REPOSITORY=docker.io/komune/ -C c2-sandbox -e VERSION=$(VERSION)

## chaincode-api
docker-chaincode-api-gateway-build:
	VERSION=$(VERSION) ./gradlew build ${CHAINCODE_APP_PACKAGE} --imageName ${CHAINCODE_APP_IMG} -x test

docker-chaincode-api-gateway-stage:
	@docker tag ${CHAINCODE_APP_IMG} ghcr.io/komune-io/${CHAINCODE_APP_IMG}
	@docker push ghcr.io/komune-io/${CHAINCODE_APP_IMG}

docker-chaincode-api-gateway-promote:
	@docker tag ${CHAINCODE_APP_IMG} docker.io/komune/${CHAINCODE_APP_IMG}
	@docker push docker.io/komune/${CHAINCODE_APP_IMG}
