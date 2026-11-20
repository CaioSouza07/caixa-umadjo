#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"

ADMIN_NAME="admin"
ADMIN_EMAIL="admin@email.com"

if [[ $# -ne 1 ]]; then
  echo "Uso: ./scripts/db-create-admin.sh <senha>" >&2
  exit 1
fi

PASSWORD="$1"

if (( ${#PASSWORD} < 6 )); then
  echo "A senha deve ter no mínimo 6 caracteres." >&2
  exit 1
fi

if (( $(printf '%s' "$PASSWORD" | wc -c) > 72 )); then
  echo "A senha deve ter no máximo 72 bytes (acentos e emojis contam como 2 a 4)." >&2
  exit 1
fi

if [[ -z "$(compose ps --status running --quiet postgres)" ]]; then
  echo "O banco não está rodando (rode ./scripts/db-up.sh)." >&2
  exit 1
fi

psql_exec() {
  compose exec -T postgres psql -q -v ON_ERROR_STOP=1 \
    -U "$(env_value DATASOURCE_USERNAME)" \
    -d "$(env_value POSTGRES_DB)" \
    "$@"
}

if [[ "$(psql_exec -tAc "SELECT to_regclass('public.users') IS NOT NULL")" != "t" ]]; then
  echo "A tabela users ainda não existe. Suba a API uma vez (./scripts/up.sh ou mvn spring-boot:run) para o Flyway criar as tabelas." >&2
  exit 1
fi

RESULT="$(psql_exec -tA -v name="$ADMIN_NAME" -v email="$ADMIN_EMAIL" -v password="$PASSWORD" <<'SQL'
SELECT NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pgcrypto') AS drop_pgcrypto \gset
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, name, email, password, role, active)
VALUES (gen_random_uuid(), :'name', :'email', crypt(:'password', gen_salt('bf', 10)), 'ADMIN', true)
ON CONFLICT (email) DO UPDATE
  SET password = EXCLUDED.password, role = 'ADMIN', active = true
RETURNING CASE WHEN xmax = 0 THEN 'criado' ELSE 'atualizado' END AS result \gset

\if :drop_pgcrypto
DROP EXTENSION pgcrypto;
\endif

\echo :result
SQL
)"

echo "Admin $RESULT: $ADMIN_EMAIL"
