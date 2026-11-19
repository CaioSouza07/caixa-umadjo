#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

if [[ -z "$(compose ps --all --quiet)" ]]; then
  echo "Nenhum container do projeto (rode ./scripts/db-up.sh ou ./scripts/up.sh)."
  exit 0
fi

compose ps --all --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}"
