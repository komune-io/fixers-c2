ARG VERSION_FABRIC=2.5.7
ARG SSM_VERSION

FROM hyperledger/fabric-tools:${VERSION_FABRIC}

ARG DOCKER_REPOSITORY
ARG COOP_PEER
ARG COOP_PEER_DOMAIN
ARG COOP_PEER_MSP
ARG CLI_USER
ARG COOP_CA_DOMAIN

ENV FABRIC_LOGGING_SPEC="info"
ENV CORE_CHAINCODE_LOGGING_LEVEL="info"
ENV CORE_PEER_ID="cli.${COOP_PEER_DOMAIN}"
ENV CORE_PEER_ADDRESS="${COOP_PEER}.${COOP_PEER_DOMAIN}:7051"
ENV CORE_PEER_LOCALMSPID="${COOP_PEER_MSP}"
ENV CORE_PEER_MSPCONFIGPATH="/etc/hyperledger/user/msp"
ENV CORE_CHAINCODE_KEEPALIVE="10"
ENV CORE_PEER_TLS_ENABLED="true"
ENV CORE_PEER_TLS_KEY_FILE="/etc/hyperledger/user/tls/client.key"
ENV CORE_PEER_TLS_CERT_FILE="/etc/hyperledger/user/tls/client.crt"
ENV CORE_PEER_TLS_ROOTCERT_FILE="/etc/hyperledger/user/tls/ca.crt"
ENV GOPATH="/opt/gopath"

COPY ./config/fabric/crypto-config/peerOrganizations/${COOP_PEER_DOMAIN}/users/${CLI_USER}@${COOP_PEER_DOMAIN}/ /etc/hyperledger/user
COPY ./config/fabric/crypto-config/ordererOrganizations/${COOP_CA_DOMAIN}/tlsca/ /etc/hyperledger/orderer/
COPY ./config/fabric/config /etc/hyperledger/config

COPY ./config/chaincode /opt/c2-sandbox/chaincode

COPY ./config/user /opt/c2-sandbox/user
COPY ./config/.env /opt/c2-sandbox

ENV PATH="/opt/c2-sandbox/chaincode/bin:${PATH}"

ENV SSM_PATH="/opt/chaincode/ssm"
COPY --from=c2-chaincode-ssm:__SSM_VERSION__ ${SSM_PATH}/ssm-*.pak ${SSM_PATH}/ssm.pak
COPY --from=c2-chaincode-ssm:__SSM_VERSION__ ${SSM_PATH}/env_ssm ${SSM_PATH}/env_ssm
COPY --from=c2-chaincode-ssm:__SSM_VERSION__ ${SSM_PATH}/bin ${SSM_PATH}/bin


ENV EX02_PATH="/opt/chaincode/ex02"
COPY --from=c2-chaincode-ex02:__EX02_VERSION__ ${EX02_PATH}/ex02-*.pak ${EX02_PATH}/ex02.pak
COPY --from=c2-chaincode-ex02:__EX02_VERSION__ ${EX02_PATH}/env_ex02 ${EX02_PATH}/env_ex02
