#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
DB_COMPOSE="$ROOT_DIR/infra/docker-compose.yml"
APP_COMPOSE="$ROOT_DIR/infra/docker-compose.app.yml"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Arquivo .env não encontrado em $ROOT_DIR (crie a partir do .env.example)." >&2
  exit 1
fi

env_value() {
  grep -E "^$1=" "$ENV_FILE" | head -n 1 | cut -d= -f2- | tr -d '\r'
}

compose() {
  docker compose -f "$DB_COMPOSE" -f "$APP_COMPOSE" --env-file "$ENV_FILE" "$@"
}
