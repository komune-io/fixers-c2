#!/bin/sh
set -e

SOCK=/host/var/run/docker.sock
if [ -S "$SOCK" ]; then
  SOCK_GID=$(stat -c '%g' "$SOCK")
  if ! getent group "$SOCK_GID" >/dev/null 2>&1; then
    groupadd -g "$SOCK_GID" dockerhost
  fi
  usermod -aG "$SOCK_GID" fabric
fi

chown -R fabric:fabric /var/hyperledger

exec gosu fabric "$@"
