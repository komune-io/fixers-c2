ARG VERSION_FABRIC=2.5.16

FROM hyperledger/fabric-peer:${VERSION_FABRIC}

RUN useradd -u 1000 -U -M -s /usr/sbin/nologin fabric

# gosu drops root privileges after entrypoint.sh matches this user's supplementary
# group to the mounted host docker.sock's GID, which varies per host/CI runner.
# hadolint ignore=DL3008
RUN apt-get update && apt-get install -y --no-install-recommends gosu && rm -rf /var/lib/apt/lists/*

ARG COOP_PEER
ARG COOP_PEER_DOMAIN
ARG COOP_PEER_MSP

ENV CORE_PEER_ID="${COOP_PEER}.${COOP_PEER_DOMAIN}"
ENV CORE_PEER_LOCALMSPID="${COOP_PEER_MSP}"
ENV CORE_PEER_MSPCONFIGPATH="/etc/hyperledger/msp"
ENV CORE_PEER_ADDRESS="${COOP_PEER}.${COOP_PEER_DOMAIN}:7051"
ENV CORE_PEER_TLS_ENABLED="true"
ENV CORE_PEER_TLS_KEY_FILE="/etc/hyperledger/tls/server.key"
ENV CORE_PEER_TLS_CERT_FILE="/etc/hyperledger/tls/server.crt"
ENV CORE_PEER_TLS_ROOTCERT_FILE="/etc/hyperledger/tls/ca.crt"
ENV CORE_PEER_GOSSIP_EXTERNALENDPOINT="${COOP_PEER}.${COOP_PEER_DOMAIN}:7051"
ENV FABRIC_LOGGING_SPEC="info"
ENV CORE_CHAINCODE_LOGGING_LEVEL="info"

RUN mkdir -p /var/hyperledger/production && chown -R fabric:fabric /var/hyperledger

COPY --chown=fabric:fabric ./config/fabric/crypto-config/peerOrganizations/${COOP_PEER_DOMAIN}/peers/${COOP_PEER}.${COOP_PEER_DOMAIN}/msp /etc/hyperledger/msp
COPY --chown=fabric:fabric ./config/fabric/crypto-config/peerOrganizations/${COOP_PEER_DOMAIN}/peers/${COOP_PEER}.${COOP_PEER_DOMAIN}/tls /etc/hyperledger/tls

COPY --chmod=755 c2-sandbox-peer/entrypoint.sh /usr/local/bin/entrypoint.sh

# trivy:ignore:DS-0002 -- this container must start as root so entrypoint.sh can
# match the fabric user's supplementary group to the mounted host docker.sock's
# GID (varies per host/CI runner) before dropping to non-root via gosu. See
# entrypoint.sh — the actual `peer node start` process runs as uid=1000(fabric).
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["peer","node","start"]
