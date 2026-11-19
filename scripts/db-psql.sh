#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

if [[ -z "$(compose ps --status running --quiet postgres)" ]]; then
  echo "O banco não está rodando (rode ./scripts/db-up.sh)." >&2
  exit 1
fi

compose exec postgres psql \
  -U "$(env_value DATASOURCE_USERNAME)" \
  -d "$(env_value POSTGRES_DB)" \
  "$@"
