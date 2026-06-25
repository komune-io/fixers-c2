#!/bin/bash
# Idempotently set the Orderer block-cutting params (BatchSize / BatchTimeout) on $CHANNEL.
#
# These values live in the channel's Orderer group, whose mod_policy is /Channel/Orderer/Admins,
# so the update MUST be signed by the orderer-org admin (BlockchainLANOrdererMSP) — not the peer
# admin the cli normally runs as. The orderer admin MSP is baked into the image at
# /etc/hyperledger/orderer-admin/msp (see c2-sandbox-cli/Dockerfile).
#
# Re-running is safe: if the on-chain values already match the desired ones, it does nothing.
#
# Override any value via the container environment (e.g. compose `environment:`):
#   PREFERRED_MAX_BYTES  (default 5242880  = 5 MiB)
#   ABSOLUTE_MAX_BYTES   (default 51380224 = 49 MiB)
#   MAX_MESSAGE_COUNT    (default 1024)
#   BATCH_TIMEOUT        (default 2s)
set -e

source /opt/c2-sandbox/chaincode/bin/env_chaincode   # ORDERER_ADDR, ORDERER_CERT, CHANNEL

PREFERRED_MAX_BYTES="${PREFERRED_MAX_BYTES:-5242880}"
ABSOLUTE_MAX_BYTES="${ABSOLUTE_MAX_BYTES:-51380224}"
MAX_MESSAGE_COUNT="${MAX_MESSAGE_COUNT:-1024}"
BATCH_TIMEOUT="${BATCH_TIMEOUT:-2s}"
ORDERER_ADMIN_MSP="${ORDERER_ADMIN_MSP:-/etc/hyperledger/orderer-admin/msp}"
ORDERER_MSPID="${ORDERER_MSPID:-BlockchainLANOrdererMSP}"

bs='.channel_group.groups.Orderer.values.BatchSize.value'
bt='.channel_group.groups.Orderer.values.BatchTimeout.value'

WORK="$(mktemp -d)"; cd "$WORK"

echo "[batch-config] fetching current config of ${CHANNEL}"
peer channel fetch config config_block.pb -o "${ORDERER_ADDR}" -c "${CHANNEL}" --tls --cafile "${ORDERER_CERT}"
configtxlator proto_decode --input config_block.pb --type common.Block \
  | jq '.data.data[0].payload.data.config' > config.json

cur_pref=$(jq -r "${bs}.preferred_max_bytes // 0" config.json)
cur_abs=$(jq -r "${bs}.absolute_max_bytes // 0" config.json)
cur_max=$(jq -r "${bs}.max_message_count // 0" config.json)
cur_to=$(jq -r "${bt}.timeout // \"\"" config.json)

if [ "$cur_pref" = "$PREFERRED_MAX_BYTES" ] && [ "$cur_abs" = "$ABSOLUTE_MAX_BYTES" ] \
   && [ "$cur_max" = "$MAX_MESSAGE_COUNT" ] && [ "$cur_to" = "$BATCH_TIMEOUT" ]; then
  echo "[batch-config] already current (pref=$cur_pref abs=$cur_abs max=$cur_max timeout=$cur_to) — skipping"
  exit 0
fi
echo "[batch-config] updating: pref $cur_pref->$PREFERRED_MAX_BYTES  abs $cur_abs->$ABSOLUTE_MAX_BYTES  max $cur_max->$MAX_MESSAGE_COUNT  timeout $cur_to->$BATCH_TIMEOUT"

jq --argjson pref "$PREFERRED_MAX_BYTES" --argjson abs "$ABSOLUTE_MAX_BYTES" \
   --argjson mmc "$MAX_MESSAGE_COUNT" --arg to "$BATCH_TIMEOUT" "
     ${bs}.preferred_max_bytes=\$pref
   | ${bs}.absolute_max_bytes=\$abs
   | ${bs}.max_message_count=\$mmc
   | ${bt}.timeout=\$to" config.json > modified.json

configtxlator proto_encode --input config.json   --type common.Config > config.pb
configtxlator proto_encode --input modified.json --type common.Config > modified.pb
configtxlator compute_update --channel_id "${CHANNEL}" --original config.pb --updated modified.pb > update.pb
configtxlator proto_decode --input update.pb --type common.ConfigUpdate > update.json

# wrap the ConfigUpdate in a CONFIG_UPDATE (type 2) envelope; peer channel update signs + submits it
jq -n --slurpfile cu update.json --arg ch "${CHANNEL}" \
  '{payload:{header:{channel_header:{channel_id:$ch,type:2}},data:{config_update:$cu[0]}}}' \
  > update_envelope.json
configtxlator proto_encode --input update_envelope.json --type common.Envelope > update_envelope.pb

echo "[batch-config] signing as ${ORDERER_MSPID} and submitting update"
CORE_PEER_LOCALMSPID="${ORDERER_MSPID}" CORE_PEER_MSPCONFIGPATH="${ORDERER_ADMIN_MSP}" \
  peer channel update -f update_envelope.pb -c "${CHANNEL}" -o "${ORDERER_ADDR}" --tls --cafile "${ORDERER_CERT}"
echo "[batch-config] done"
