#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

echo "Antes:"
docker system df
echo

echo "Removendo imagens <none> antigas e cache de build sem uso..."
docker image prune --force
docker builder prune --force
echo

echo "Depois:"
docker system df
