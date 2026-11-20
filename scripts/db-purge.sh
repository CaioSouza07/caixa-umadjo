#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

if [[ "${1:-}" != "-y" ]]; then
  echo "Isso para tudo (banco, API e front) e APAGA todos os dados do banco."
  read -r -p "Tem certeza? [s/N] " answer
  if [[ ! "$answer" =~ ^[sS]$ ]]; then
    echo "Cancelado."
    exit 0
  fi
fi

compose down --volumes
echo "Dados do banco apagados. No próximo db-up/up o Flyway recria as tabelas do zero."
