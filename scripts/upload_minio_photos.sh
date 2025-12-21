#!/usr/bin/env bash
set -euo pipefail

# Upload local files to MinIO using mc inside a disposable container.
# Env overrides:
#   PHOTOS_DIR         - local folder with files to upload (default: assets/photos)
#   MINIO_ENDPOINT     - MinIO endpoint (default: http://localhost:9000)
#   MINIO_ACCESS_KEY   - MinIO access key (default: minioadmin)
#   MINIO_SECRET_KEY   - MinIO secret key (default: minioadmin)
#   MINIO_BUCKET       - bucket name (default: media)

PHOTOS_DIR="${PHOTOS_DIR:-assets/photos}"
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9000}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-minioadmin}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-minioadmin}"
MINIO_BUCKET="${MINIO_BUCKET:-media}"

if [ ! -d "$PHOTOS_DIR" ]; then
  echo "Photos directory not found: $PHOTOS_DIR" >&2
  exit 1
fi

echo "Uploading from $PHOTOS_DIR to $MINIO_ENDPOINT/$MINIO_BUCKET ..."

docker run --rm \
  --network host \
  -v "$(cd "$PHOTOS_DIR" && pwd)":/photos \
  minio/mc \
  sh -c "
    mc alias set local ${MINIO_ENDPOINT} ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY} &&
    mc mb --ignore-existing local/${MINIO_BUCKET} &&
    mc mirror --overwrite /photos local/${MINIO_BUCKET}
  "

echo "Done. Objects available under ${MINIO_ENDPOINT}/${MINIO_BUCKET}/<filename>"
