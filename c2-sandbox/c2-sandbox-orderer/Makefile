include ../../.env_version
include ../config/.env
export

FABRIC_ORDERER_NAME		:= c2-sandbox-orderer
FABRIC_ORDERER_IMG		:= ${FABRIC_ORDERER_NAME}:${VERSION}
FABRIC_ORDERER_LATEST 	:= ${FABRIC_ORDERER_NAME}:latest

.PHONY: lint build test publish promote

package: lint build test push

test:
	@echo No test to run

lint:
	docker run --rm -i hadolint/hadolint < Dockerfile

build:
	docker build \
		 --platform=linux/amd64 \
		 --no-cache \
    	--build-arg VERSION_FABRIC=$$VERSION_FABRIC \
    	--build-arg COOP_CA_DOMAIN=$$orderer_org \
    	--build-arg COOP_ORDERER_HOSTNAME=$$orderer_hostname \
    	-f Dockerfile \
    	-t ${FABRIC_ORDERER_IMG} ..

push:
	@docker tag ${FABRIC_ORDERER_IMG} ${DOCKER_REPOSITORY}${FABRIC_ORDERER_IMG}
	@docker push ${DOCKER_REPOSITORY}${FABRIC_ORDERER_IMG}
