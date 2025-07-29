VERSION = $(shell cat VERSION)

.PHONY: lint build test publish promote

## New
lint:
	@make -f make_libs.mk lint
	@make -f make_docker.mk lint

build:
	@make -f make_libs.mk build
	@make -f make_docker.mk build

test-pre:
	@make -f make_libs.mk test-pre

test:
	@make -f make_libs.mk test
	@make -f make_docker.mk test

stage:
	@make -f make_libs.mk stage
	@make -f make_docker.mk stage


promote:
	@make -f libs.mk promote
	@make -f docker.mk promote

## DOCKER-COMPOSE DEV ENVIRONMENT
include infra/docker-compose/dev-compose.mk
