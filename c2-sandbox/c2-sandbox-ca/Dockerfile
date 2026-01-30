ARG VERSION_FABRIC_CA=1.5.7

FROM hyperledger/fabric-ca:${VERSION_FABRIC_CA}

ARG COOP_CA_HOSTNAME
ARG COOP_PEER_DOMAIN
ARG ca__CA_KEYFILE
ARG ca__TLS_KEYFILE
ARG ca__ADMIN
# hadolint ignore=DL3036
ARG ca__PASSWD

RUN echo ${COOP_CA_HOSTNAME} && \
     echo ${COOP_PEER_DOMAIN} && \
     echo ${ca__CA_KEYFILE} && \
     echo ${ca__TLS_KEYFILE} && \
     echo ${ca__ADMIN} && \
     echo ${ca__PASSWD}

# Sandbox dev credentials only
ENV CA_ADMIN="${ca__ADMIN}"
ENV CA_PASSWD="${ca__PASSWD}"
ENV FABRIC_CA_HOME="/etc/hyperledger/fabric-ca-server"
ENV FABRIC_CA_SERVER_CA_NAME="${COOP_CA_HOSTNAME}"
ENV FABRIC_CA_SERVER_CA_CERTFILE="/etc/hyperledger/${COOP_CA_HOSTNAME}/${COOP_CA_HOSTNAME}.${COOP_PEER_DOMAIN}-cert.pem"
ENV FABRIC_CA_SERVER_CA_KEYFILE="/etc/hyperledger/${COOP_CA_HOSTNAME}/${ca__CA_KEYFILE}"
ENV FABRIC_CA_SERVER_TLS_ENABLED="true"
ENV FABRIC_CA_SERVER_TLS_CERTFILE="/etc/hyperledger/tlsca/tlsca.${COOP_PEER_DOMAIN}-cert.pem"
ENV FABRIC_CA_SERVER_TLS_KEYFILE="/etc/hyperledger/tlsca/${ca__TLS_KEYFILE}"

COPY ../config/fabric/crypto-config/peerOrganizations/${COOP_PEER_DOMAIN}/ca /etc/hyperledger/ca
COPY ../config/fabric/crypto-config/peerOrganizations/${COOP_PEER_DOMAIN}/tlsca /etc/hyperledger/tlsca

CMD ["sh", "-c", "fabric-ca-server start -b \"$CA_ADMIN:$CA_PASSWD\""]
