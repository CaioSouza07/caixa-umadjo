#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

echo "Parando o banco (os dados são mantidos)."
echo "API e front também param, se estiverem rodando, porque dependem dele."
compose down
