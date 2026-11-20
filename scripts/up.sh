#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

echo "Subindo API e front (o banco sobe junto, se ainda não estiver rodando)..."
if ! compose up -d --build --wait api web; then
  echo "Falha ao subir. Veja os logs: docker logs caixa-umadjo-api" >&2
  exit 1
fi

echo
echo "Pronto:"
echo "  Front:  http://localhost:3000"
echo "  API:    http://localhost:8080"
echo "  Health: http://localhost:8080/actuator/health"
