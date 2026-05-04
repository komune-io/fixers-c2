include ../../.env_version

EX02_CHAINCODE_DOCKERFILE 	:= Dockerfile
EX02_CHAINCODE_NAME 		:= c2-chaincode-ex02
EX02_CHAINCODE_IMG 			:= ${EX02_CHAINCODE_NAME}:${VERSION}

.PHONY: lint build test publish promote

test:
	echo 'No Lint'

# chaincode-ex02
lint:
	docker run --rm -i hadolint/hadolint < Dockerfile

build:
	docker buildx build --output type=docker,compression=gzip,force-compression=true \
 		--platform=linux/amd64 \
		--no-cache \
		--build-arg VERSION=${VERSION} \
		--build-arg VERSION_FABRIC=${VERSION_FABRIC} \
		--build-arg VERSION_ALPINE=${VERSION_ALPINE} \
		-f ${EX02_CHAINCODE_DOCKERFILE} \
		-t ${EX02_CHAINCODE_IMG} .

push:
	@bash ../../infra/docker-push.sh ${EX02_CHAINCODE_IMG} ${DOCKER_REPOSITORY}${EX02_CHAINCODE_IMG}

inspect:
	@docker run -it ${EX02_CHAINCODE_IMG} sh
