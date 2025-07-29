
include ../.env_version
export

.PHONY: lint build test publish promote

lint:
	make lint -C chaincode-ex02
	make lint -C chaincode-ssm

build:
	make build -C chaincode-ex02
	make build -C chaincode-ssm

push:
	make push -C chaincode-ex02
	make push -C chaincode-ssm

