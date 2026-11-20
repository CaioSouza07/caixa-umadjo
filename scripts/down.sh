#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

echo "Parando API e front (o banco continua rodando)..."
compose rm --stop --force api web
