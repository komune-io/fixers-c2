VERSION = $(shell cat VERSION)
SANDBOX_NETWORK = $(shell sed -n 's/^DOCKER_NETWORK=//p' infra/docker-compose/.env_dev)

# Waits for a sandbox init container to finish, prints its logs, and fails on a non-zero exit. `logs -f` used to
# do the waiting, but the Docker Compose of GitHub's ubuntu-26.04 runner keeps following a container that has already
# exited, and the job hung for an hour.
define await-init
	@code=$$(docker wait cli-init-$(1)-bclan-$(SANDBOX_NETWORK)); \
		make dev c2-sandbox-$(1) log; \
		test "$$code" = 0 || { echo "cli-init-$(1) exited with $$code"; exit 1; }
endef

.PHONY: clean lint build test test-pre check publish promote verify-metadata verify-metadata-dry

clean:
	./gradlew clean

lint:
	./gradlew detekt

# `clean` runs alone: in one invocation the root `clean` deletes build/js while the subprojects'
# Kotlin/JS tasks write their packages there (Unable to delete directory … New files were found).
build:
	./gradlew clean
	VERSION=$(VERSION) ./gradlew build publishToMavenLocal -Dorg.gradle.parallel=true -x test -x jvmTest -x jsTest -x jsBrowserTest -x kotlinStoreYarnLock

test-pre:
	@#make dev pull
	@make dev up
	@echo "///////////////////////2"
	$(call await-init,ssm)
	@echo "///////////////////////3"
	$(call await-init,ex02)
	@echo "///////////////////////4"
	@make dev up
	@echo "///////////////////////6"
	$(call await-init,ssm)
	@echo "///////////////////////7"
	$(call await-init,ex02)
	@echo "///////////////////////8"
	sudo echo "127.0.0.1 ca.bc-coop.bclan" | sudo tee -a /etc/hosts
	sudo echo "127.0.0.1 peer0.bc-coop.bclan" | sudo tee -a /etc/hosts
	sudo echo "127.0.0.1 orderer.bclan" | sudo tee -a /etc/hosts

test:
	./gradlew allTests test

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
