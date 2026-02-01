ARG VERSION=latest
ARG DOCKER_REPOSITORY=

FROM ${DOCKER_REPOSITORY}c2-chaincode-api-gateway:${VERSION}

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
ENV coop_user_password="${ca__PASSWD}"
ENV coop_config_file="file:/opt/rest-coop/config.json"
ENV coop_config_crypto="file:/opt/rest-coop"

COPY ./config/fabric/config.json /opt/rest-coop/config.json
COPY ./config/fabric/crypto-config /opt/rest-coop/crypto-config
