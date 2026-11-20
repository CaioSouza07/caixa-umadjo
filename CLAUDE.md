# caixa-umadjo

Monorepo com API (Spring Boot) e front web (React).

## Estrutura
- `apps/api` — Spring Boot 4, Java 21, Maven, PostgreSQL, Flyway, Spring Security, Lombok
- `apps/web` — React 19 + Vite, JavaScript (JSX), ESLint

## Comandos
### API (em `apps/api`)
- Rodar: `mvn spring-boot:run`
- Testes: `mvn test` (unitários + `@WebMvcTest`; não precisam de banco nem de `.env`)
### Web (em `apps/web`)
- Dev: `npm run dev`
- Build: `npm run build`
- Lint: `npm run lint`

### Docker (na raiz do repo)
- `infra/docker-compose.yml` → só o Postgres (uso normal de dev: API e web rodam localmente).
- `infra/docker-compose.app.yml` → API (:8080) + web (:3000); sempre combinado com o de cima.
- Scripts em `scripts/` (bash; rodar no WSL, de qualquer pasta):
  - `up.sh` → sobe API + front (e o banco, se preciso) · `down.sh` → para API + front, banco segue
  - `db-up.sh` → sobe só o banco · `db-down.sh` → para tudo, mantém os dados
  - `db-purge.sh` → para tudo e apaga o volume do banco (pede confirmação; `-y` pula)
  - `logs.sh [api|web|postgres]` → segue os logs (sem argumento: todos)
  - `status.sh` → containers do projeto e se estão healthy
  - `db-psql.sh [args do psql]` → psql dentro do container (ex.: `-c "select ..."`)
  - `db-create-admin.sh <senha>` → cria/atualiza o admin `admin@email.com` (hash BCrypt via pgcrypto)
  - `clean.sh` → remove imagens `<none>` e cache de build sem uso
- Documentação de uso: `scripts/README.md`. Script novo: `git add --chmod=+x`.
- `scripts/_common.sh` centraliza o `docker compose` com os dois arquivos + `--env-file .env`.
- Dockerfiles ficam em cada app (`apps/api/Dockerfile`, `apps/web/Dockerfile`).

### CI (`.github/workflows/ci.yml`)
- Roda em todo PR, push na `main` e manual. Jobs: API (`mvn verify`), Web (lint + build),
  Scripts (`shellcheck --exclude=SC1091`), Smoke test (`up.sh` + health `UP` + front 200).
- O smoke cria o `.env` a partir do `.env.example` e falha se sobrar variável vazia:
  variável nova no `.env.example` sem valor padrão precisa ser preenchida no step "Criar .env de CI".
- Smoke é só "sobe e responde": não adicionar testes de funcionalidade nele.

## Ambiente
- A API lê o `.env` da raiz do repo (`../../.env`). Variáveis obrigatórias:
  `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`,
  `SECURITY_TOKEN_JWT_API`, `SECURITY_CORS_ORIGINS`
  (+ `POSTGRES_DB`, `POSTGRES_PORT`, usadas pelos compose de `infra/`)
- Precisa de um PostgreSQL rodando: usar o compose de `infra/` (ver Docker acima).

## Arquitetura da API
- `domain/<entidade>/` → `dto`, `entity`, `mapper`, `repository`, `service`
- `infra/` → preocupações transversais (`authentication`, `exception`)
- Exceções customizadas ficam em `infra/exception/exceptions`

## Convenções
- Migrations Flyway em `src/main/resources/db/migration`, formato `V<n>__descricao.sql`.
  Nunca editar uma migration já aplicada: criar uma nova.
- Commits no estilo Conventional Commits (`feat:`, `fix:`...), de preferência em inglês
- Branches: `feature/<descricao>`
