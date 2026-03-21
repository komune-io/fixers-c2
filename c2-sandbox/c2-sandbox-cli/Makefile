include ../../.env_version
include ../config/.env
export

FABRIC_CLI_NAME			:= c2-sandbox-cli
FABRIC_CLI_IMG			:= ${FABRIC_CLI_NAME}:${VERSION}
FABRIC_CLI_LATEST 		:= ${FABRIC_CLI_NAME}:latest

.PHONY: lint build test publish promote

package: clean lint build test push

clean:
	rm -fr build

lint:
	docker run --rm -i hadolint/hadolint < Dockerfile

test:
	@echo No test to run

build:
	-@mkdir build
	@sed 's/__SSM_VERSION__/${VERSION}/' Dockerfile > build/Dockerfile_tmp
	@sed 's/__EX02_VERSION__/${VERSION}/' build/Dockerfile_tmp > build/Dockerfile
	@docker buildx build --output type=docker,compression=gzip,force-compression=true \
 		--platform=linux/amd64 \
		--no-cache \
    	--build-arg SSM_VERSION=$$SSM_VERSION \
    	--build-arg VERSION_FABRIC=$$VERSION_FABRIC \
    	--build-arg VERSION_CHAINCODE_API=$$VERSION \
    	--build-arg COOP_CA_DOMAIN=$$orderer_org \
    	--build-arg COOP_PEER=peer0 \
    	--build-arg COOP_PEER_DOMAIN=$$cli_ORGA \
    	--build-arg COOP_PEER_MSP=$$peer_msp \
    	--build-arg CLI_USER=$$cli_user \
    	-f build/Dockerfile \
    	-t ${FABRIC_CLI_IMG} ..

push:
	@bash ../../infra/docker-push.sh ${FABRIC_CLI_IMG} ${DOCKER_REPOSITORY}${FABRIC_CLI_IMG}
