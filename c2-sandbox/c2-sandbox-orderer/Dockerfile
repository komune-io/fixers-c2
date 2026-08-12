ARG VERSION_FABRIC=2.5.16

FROM hyperledger/fabric-orderer:${VERSION_FABRIC}

RUN useradd -u 1000 -U -M -s /usr/sbin/nologin fabric

ARG COOP_CA_DOMAIN
ARG COOP_ORDERER_HOSTNAME

ENV ORDERER_GENERAL_LOGLEVEL="info"
ENV ORDERER_GENERAL_LISTENADDRESS="0.0.0.0"
ENV ORDERER_GENERAL_GENESISMETHOD="file"
ENV ORDERER_GENERAL_GENESISFILE="/etc/hyperledger/config/genesis.block"
ENV ORDERER_GENERAL_LOCALMSPID="BlockchainLANOrdererMSP"
ENV ORDERER_GENERAL_LOCALMSPDIR="/etc/hyperledger/msp/"
ENV ORDERER_GENERAL_TLS_ENABLED="true"
# Path to the TLS key file, not a secret value
# hadolint ignore=DL3064
ENV ORDERER_GENERAL_TLS_PRIVATEKEY="/etc/hyperledger/tls/server.key"
ENV ORDERER_GENERAL_TLS_CERTIFICATE="/etc/hyperledger/tls/server.crt"
ENV ORDERER_GENERAL_TLS_ROOTCAS="[/etc/hyperledger/tls/ca.crt]"

RUN mkdir -p /var/hyperledger/production/orderer && chown -R fabric:fabric /var/hyperledger

COPY --chown=fabric:fabric ./config/fabric/config /etc/hyperledger/config
COPY --chown=fabric:fabric ./config/fabric/crypto-config/ordererOrganizations/${COOP_CA_DOMAIN}/orderers/${COOP_ORDERER_HOSTNAME}.${COOP_CA_DOMAIN}/msp /etc/hyperledger/msp
COPY --chown=fabric:fabric ./config/fabric/crypto-config/ordererOrganizations/${COOP_CA_DOMAIN}/orderers/${COOP_ORDERER_HOSTNAME}.${COOP_CA_DOMAIN}/tls /etc/hyperledger/tls

# DL3066 ignored: username is intentional; uid 1000 is fixed by the useradd above.
# hadolint ignore=DL3066
USER fabric

CMD ["orderer"]
