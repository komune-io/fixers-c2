ARG VERSION_FABRIC

FROM hyperledger/fabric-orderer:${VERSION_FABRIC}

ARG COOP_CA_DOMAIN
ARG COOP_ORDERER_HOSTNAME

ENV ORDERER_GENERAL_LOGLEVEL info
ENV ORDERER_GENERAL_LISTENADDRESS 0.0.0.0
ENV ORDERER_GENERAL_GENESISMETHOD file
ENV ORDERER_GENERAL_GENESISFILE /etc/hyperledger/config/genesis.block
ENV ORDERER_GENERAL_LOCALMSPID BlockchainLANOrdererMSP
ENV ORDERER_GENERAL_LOCALMSPDIR /etc/hyperledger/msp/
ENV ORDERER_GENERAL_TLS_ENABLED true
ENV ORDERER_GENERAL_TLS_PRIVATEKEY /etc/hyperledger/tls/server.key
ENV ORDERER_GENERAL_TLS_CERTIFICATE /etc/hyperledger/tls/server.crt
ENV ORDERER_GENERAL_TLS_ROOTCAS [/etc/hyperledger/tls/ca.crt]


COPY ./config/fabric/config /etc/hyperledger/config
COPY ./config/fabric/crypto-config/ordererOrganizations/${COOP_CA_DOMAIN}/orderers/${COOP_ORDERER_HOSTNAME}.${COOP_CA_DOMAIN}/msp /etc/hyperledger/msp
COPY ./config/fabric/crypto-config/ordererOrganizations/${COOP_CA_DOMAIN}/orderers/${COOP_ORDERER_HOSTNAME}.${COOP_CA_DOMAIN}/tls /etc/hyperledger/tls

CMD ["orderer"]
