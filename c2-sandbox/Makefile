VERSION = $(shell cat ../VERSION)
include ../.env_version
export

.PHONY: lint build test publish promote

lint:
	make lint -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-ca
	make lint -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-cli
	make lint -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-orderer
	make lint -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-peer
	make lint -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-ssm-gateway

build:
	make build -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-ca
	make build -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-cli
	make build -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-orderer
	make build -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-peer
	make build -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-ssm-gateway

push:
	make push -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-ca
	make push -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-cli
	make push -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-orderer
	make push -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-peer
	make push -e VERSION=${VERSION} -e DOCKER_REPOSITORY=${DOCKER_REPOSITORY} -C c2-sandbox-ssm-gateway
