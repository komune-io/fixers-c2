#!/usr/bin/env bash
set -euo pipefail

SOURCE_IMG="$1"
TARGET_IMG="$2"
MAX_RETRIES="${DOCKER_PUSH_RETRIES:-3}"
BACKOFF=5

docker tag "$SOURCE_IMG" "$TARGET_IMG"

for attempt in $(seq 1 "$MAX_RETRIES"); do
    if docker push "$TARGET_IMG" 2>&1; then
        exit 0
    fi
    if [ "$attempt" -lt "$MAX_RETRIES" ]; then
        echo "docker push failed (attempt $attempt/$MAX_RETRIES), retrying in ${BACKOFF}s..."
        sleep "$BACKOFF"
        BACKOFF=$((BACKOFF * 3))
    fi
done

echo "docker push failed after $MAX_RETRIES attempts"
exit 1
