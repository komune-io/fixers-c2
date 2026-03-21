include ../../.env_version
include ../config/.env
export

FABRIC_SSM_REST_NAME	:= c2-sandbox-ssm-gateway
FABRIC_SSM_REST_IMG		:= ${FABRIC_SSM_REST_NAME}:${VERSION}
FABRIC_SSM_REST_LATEST 	:= ${FABRIC_SSM_REST_NAME}:latest

.PHONY: lint build test publish promote

clean:
	@echo No test to run

lint:
	docker run --rm -i hadolint/hadolint < Dockerfile

test:
	@echo No test to run

package: lint build test push

build:
	@docker build \
	 	--platform=linux/amd64 \
	 	--no-cache \
    	--build-arg VERSION=$$VERSION \
    	--build-arg coop_channel=$$coop_channel \
    	--build-arg coop_ccid=$$coop_ccid \
		--build-arg coop_user_org=$$coop_user_org \
		--build-arg coop_endorsers=$$coop_endorsers \
    	--build-arg ca__ADMIN=$$ca__ADMIN \
    	--build-arg ca__PASSWD=$$ca__PASSWD \
    	-f Dockerfile \
    	-t ${FABRIC_SSM_REST_IMG} ..

push:
	@bash ../../infra/docker-push.sh ${FABRIC_SSM_REST_IMG} ${DOCKER_REPOSITORY}${FABRIC_SSM_REST_IMG}
