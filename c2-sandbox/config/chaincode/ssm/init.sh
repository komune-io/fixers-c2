#!/bin/bash

source /opt/c2-sandbox/chaincode/bin/env_chaincode
init_channel.sh

# Apply the orderer block-cutting params (BatchSize/BatchTimeout) to the channel.
# Idempotent: skips when on-chain values already match. Override via container env
# (PREFERRED_MAX_BYTES / ABSOLUTE_MAX_BYTES / MAX_MESSAGE_COUNT / BATCH_TIMEOUT).
set_batch_config.sh

source /opt/chaincode/ssm/env_ssm
source /opt/c2-sandbox/chaincode/ssm/env_ssm

echo "Install chaincode ${CHAINCODE_PAK}"
peer chaincode install ${CHAINCODE_PAK}

sleep 5
echo "Instantiate chaincode ${CHAINCODE}:${VERSION}  on channel ${CHANNEL}"
peer chaincode instantiate -o ${ORDERER_ADDR} --tls --cafile ${ORDERER_CERT} \
  -C ${CHANNEL} -n ${CHAINCODE} -v ${VERSION} \
  -c "$(cat $CHAINCODE_ARG_INIT)" \
  -P "OR ('BlockchainLANCoopMSP.member')" \

sleep 5
echo "Query chaincode ${CHAINCODE}"
peer chaincode query -C ${CHANNEL} -n ${CHAINCODE} -c "$(cat $CHAINCODE_ARG_TEST)"
