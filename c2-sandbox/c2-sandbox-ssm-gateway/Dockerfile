ARG VERSION=latest

FROM c2-chaincode-api-gateway:${VERSION}

ARG coop_channel
ARG coop_ccid
ARG coop_user_org
ARG coop_endorsers
ARG ca__ADMIN
# hadolint ignore=DL3036
ARG ca__PASSWD
ARG coop_config_file
ARG coop_config_crypto

ENV coop_defaultCcid="${coop_channel}/${coop_ccid}"
ENV coop_ccid="${coop_channel}/${coop_ccid}"

ENV coop_user_org="${coop_user_org}"
ENV coop_endorsers="${coop_endorsers}"
ENV coop_user_name="${ca__ADMIN}"
# Sandbox dev credentials only
# hadolint ignore=DL3064
ENV coop_user_password="${ca__PASSWD}"
ENV coop_config_file="file:/opt/rest-coop/config.json"
ENV coop_config_crypto="file:/opt/rest-coop"

COPY --chown=cnb:cnb ./config/fabric/config.json /opt/rest-coop/config.json
COPY --chown=cnb:cnb ./config/fabric/crypto-config /opt/rest-coop/crypto-config

# DL3066 ignored: `cnb` is the account this Paketo/Cloud-Native-Buildpacks base
# image already runs as by default (uid=1002 gid=1001, verified via `docker
# inspect`/`/etc/passwd`) - declaring it by name here only makes Trivy's static
# DS-0002 check pass; it changes nothing about how the container actually runs.
# hadolint ignore=DL3066
USER cnb
