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
- Subir banco: `docker compose -f infra/docker-compose.yml --env-file .env up -d`
- Subir tudo: `docker compose -f infra/docker-compose.yml -f infra/docker-compose.app.yml --env-file .env up -d --build`
- Parar: trocar `up -d` por `down` · Apagar dados do banco: `down -v`
- Dockerfiles ficam em cada app (`apps/api/Dockerfile`, `apps/web/Dockerfile`).

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
- Commits em português, no estilo Conventional Commits (`feat:`, `fix:`...)
- Branches: `feature/<descricao>`
