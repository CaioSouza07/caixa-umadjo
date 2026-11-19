#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

echo "Subindo o banco..."
compose up -d --wait postgres

echo
echo "Banco pronto em localhost:$(env_value POSTGRES_PORT) (banco: $(env_value POSTGRES_DB))"
