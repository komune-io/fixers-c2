include ../../.env_version

SSM_CHAINCODE_DOCKERFILE 	:= Dockerfile
SSM_CHAINCODE_NAME 			:= c2-chaincode-ssm
SSM_CHAINCODE_IMG 			:= ${SSM_CHAINCODE_NAME}:${VERSION}

.PHONY: lint build test publish promote

test:
	echo 'No Lint'

# chaincode-ssm
lint:
	docker run --rm -i hadolint/hadolint < Dockerfile

build:
	docker build --no-cache \
		--platform=linux/amd64 \
		--no-cache \
		--build-arg VERSION_FABRIC=${VERSION_FABRIC} \
		--build-arg VERSION_ALPINE=${VERSION_ALPINE} \
		--build-arg VERSION=${VERSION} \
		-f ${SSM_CHAINCODE_DOCKERFILE} \
		-t ${SSM_CHAINCODE_IMG} .

push:
	@bash ../../infra/docker-push.sh ${SSM_CHAINCODE_IMG} ${DOCKER_REPOSITORY}${SSM_CHAINCODE_IMG}

inspect:
	@docker run -it ${SSM_CHAINCODE_IMG} sh
