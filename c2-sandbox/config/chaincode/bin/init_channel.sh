#!/bin/bash

echo "Source env"
source env_chaincode

# Wait for peer to be ready
MAX_RETRIES=10
for i in $(seq 1 $MAX_RETRIES); do
  if peer channel list -o ${ORDERER_ADDR} --tls --cafile ${ORDERER_CERT} 2>/dev/null; then
    break
  fi
  echo "Waiting for peer to be ready... ($i/$MAX_RETRIES)"
  sleep 2
done

# Check if already joined to channel
JOINED_CHANNELS=$(peer channel list 2>/dev/null | grep -w "${CHANNEL}" || true)
if [ -n "$JOINED_CHANNELS" ]; then
  echo "Already joined to channel ${CHANNEL}, skipping channel init"
  exit 0
fi

# Check if channel exists on orderer
CHANNEL_EXISTS=$(peer channel getinfo -o ${ORDERER_ADDR} -c ${CHANNEL} --tls --cafile ${ORDERER_CERT} 2>&1 | grep -i "Blockchain info" || true)

if [ -z "$CHANNEL_EXISTS" ]; then
  echo "Create channel ${CHANNEL}"
  peer channel create -o ${ORDERER_ADDR} -c ${CHANNEL} -f /etc/hyperledger/config/${CHANNEL}.tx --tls --cafile ${ORDERER_CERT}
else
  echo "Channel ${CHANNEL} already exists, fetching genesis block"
  peer channel fetch 0 ${CHANNEL}.block -o ${ORDERER_ADDR} -c ${CHANNEL} --tls --cafile ${ORDERER_CERT}
fi

echo "Join channel ${CHANNEL}"
peer channel join -b ${CHANNEL}.block
