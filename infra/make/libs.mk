VERSION = $(shell cat VERSION)

.PHONY: clean lint build test test-pre check publish promote verify-metadata verify-metadata-dry

clean:
	./gradlew clean

lint:
	./gradlew detekt

build:
	VERSION=$(VERSION) ./gradlew clean build publishToMavenLocal -Dorg.gradle.parallel=true -x test

test-pre:
	@#make dev pull
	@make dev up
	@echo "///////////////////////2"
	@make dev c2-sandbox-ssm logs
	@echo "///////////////////////3"
	@make dev c2-sandbox-ex02 logs
	@echo "///////////////////////4"
	@make dev up
	@echo "///////////////////////6"
	@make dev c2-sandbox-ssm logs
	@echo "///////////////////////7"
	@make dev c2-sandbox-ex02 logs
	@echo "///////////////////////8"
	sudo echo "127.0.0.1 ca.bc-coop.bclan" | sudo tee -a /etc/hosts
	sudo echo "127.0.0.1 peer0.bc-coop.bclan" | sudo tee -a /etc/hosts
	sudo echo "127.0.0.1 orderer.bclan" | sudo tee -a /etc/hosts

test:
	./gradlew test

test-post:
	@make dev down

check:
	VERSION=$(VERSION) ./gradlew sonar

stage:
	VERSION=$(VERSION) ./gradlew stage

promote:
	VERSION=$(VERSION) ./gradlew promote

# Regenerates gradle/verification-metadata.xml and the exported keyring after a dependency bump.
# --dry-run runs no task (no test, no publish) but still resolves the whole `build` +
# `publishToMavenLocal` task graph, so every configuration CI touches is covered. Gradle writes the
# result next to the real files with a .dryrun suffix; they are moved into place here.
# Review the diff before committing.
verify-metadata: verify-metadata-dry
	mv gradle/verification-metadata.dryrun.xml gradle/verification-metadata.xml
	mv gradle/verification-keyring.dryrun.keys gradle/verification-keyring.keys
	mv gradle/verification-keyring.dryrun.gpg gradle/verification-keyring.gpg

# Generates the same files with the .dryrun suffix, to inspect the delta without replacing anything.
verify-metadata-dry:
	./gradlew --write-verification-metadata pgp,sha256 --export-keys --dry-run build publishToMavenLocal

.PHONY: version
version:
	@echo "$(VERSION)"
