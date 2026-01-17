#!/usr/bin/env bash

if [ -z "$BASH_VERSION" ]; then
  echo "[ERROR] Script must be run with bash: bash apply.sh <namespace> [registry] [tag]" >&2
  exit 1
fi

set -euo pipefail

ROOT_DIR=$(cd "$(dirname "$0")" && pwd)
NAMESPACE=${1:-default}
IMAGE_REGISTRY=${2:-fixly-meow.ru:5000}
IMAGE_TAG=${3:-latest}

kubectl get namespace "$NAMESPACE" >/dev/null 2>&1 || kubectl create namespace "$NAMESPACE"

kubectl apply -n "$NAMESPACE" -f "$ROOT_DIR/k8s/infra"
kubectl apply -n "$NAMESPACE" -f "$ROOT_DIR/k8s/apps"

services=(admin-service auth-service user-service gateway-server eureka-server)
for svc in "${services[@]}"; do
  image="$IMAGE_REGISTRY/degree_manager-${svc}:$IMAGE_TAG"
  echo "Updating $svc image to $image"
  kubectl set image -n "$NAMESPACE" deployment/"$svc" "$svc"="$image" --record
  kubectl rollout status -n "$NAMESPACE" deployment/"$svc"
done
