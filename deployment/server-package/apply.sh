#!/usr/bin/env bash

if [ -z "$BASH_VERSION" ]; then
  echo "[ERROR] Script must be run with bash: bash apply.sh <namespace> [registry] [tag]" >&2
  exit 1
fi

set -euo pipefail

if command -v kubectl >/dev/null 2>&1; then
  KUBECTL=(kubectl)
elif command -v minikube >/dev/null 2>&1; then
  KUBECTL=(minikube kubectl --)
else
  echo "[ERROR] Neither kubectl nor minikube is available in PATH" >&2
  exit 1
fi

run_kubectl() {
  "${KUBECTL[@]}" "$@"
}

ROOT_DIR=$(cd "$(dirname "$0")" && pwd)
NAMESPACE=${1:-default}
IMAGE_REGISTRY=${2:-fixly-meow.ru:5000}
IMAGE_TAG=${3:-latest}

run_kubectl delete deployments --all

run_kubectl get namespace "$NAMESPACE" >/dev/null 2>&1 || run_kubectl create namespace "$NAMESPACE"

run_kubectl apply -n "$NAMESPACE" -f "$ROOT_DIR/k8s/infra"
run_kubectl apply -n "$NAMESPACE" -f "$ROOT_DIR/k8s/apps"

services=(admin-service auth-service user-service gateway-server eureka-server)
for svc in "${services[@]}"; do
  image="$IMAGE_REGISTRY/degree_manager-${svc}:$IMAGE_TAG"
  echo "Updating $svc image to $image"
  run_kubectl set image -n "$NAMESPACE" deployment/"$svc" "$svc"="$image" --record
  run_kubectl rollout status -n "$NAMESPACE" deployment/"$svc"
done

run_kubectl patch svc gateway-server -p '{"spec":{"type":"NodePort","ports":[{"name":"http","port":8075,"targetPort":8075,"protocol":"TCP","nodePort":30075}]}}'
