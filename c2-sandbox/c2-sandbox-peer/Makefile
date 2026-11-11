include ../config/.env
export

FABRIC_PEER_NAME		:= c2-sandbox-peer
FABRIC_PEER_IMG			:= ${FABRIC_PEER_NAME}:${VERSION}
FABRIC_PEER_LATEST 		:= ${FABRIC_PEER_NAME}:latest

.PHONY: lint build test publish promote

package: lint build test push

lint:
	docker run --rm -i hadolint/hadolint < Dockerfile

test:
	@echo No test to run

# The apt-get step resolves through the host network: on GitHub's ubuntu-26.04 runner image the build
# network gives these glibc (Ubuntu jammy) Fabric images no working resolver ("Could not resolve
# archive.ubuntu.com"), while the same build passes on ubuntu-24.04 and Alpine images resolve on both.
build:
	@docker buildx build --output type=docker,compression=gzip,force-compression=true \
		--network=host \
		 --platform=linux/amd64 \
		 --no-cache \
		--build-arg DOCKER_REPOSITORY=$$DOCKER_REPOSITORY \
		--build-arg COOP_PEER=peer0 \
    	--build-arg COOP_PEER_MSP=$$peer_msp \
    	--build-arg COOP_PEER_DOMAIN=$$cli_ORGA \
    	-f Dockerfile \
    	-t ${FABRIC_PEER_IMG} ..

push:
	@bash ../../infra/docker-push.sh ${FABRIC_PEER_IMG} ${DOCKER_REPOSITORY}${FABRIC_PEER_IMG}
