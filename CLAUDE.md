# caixa-umadjo

Monorepo com API (Spring Boot) e front web (React).

## Estrutura
- `apps/api` — Spring Boot 4, Java 21, Maven, PostgreSQL, Flyway, Spring Security, Lombok
- `apps/web` — React 19 + Vite, JavaScript (JSX), ESLint

## Comandos
### API (em `apps/api`)
- Rodar: `mvn spring-boot:run`
- Testes: `mvn test`
### Web (em `apps/web`)
- Dev: `npm run dev`
- Build: `npm run build`
- Lint: `npm run lint`

### Docker
- [A confirmar ainda]

## Ambiente
- A API lê o `.env` da raiz do repo (`../../.env`). Variáveis obrigatórias:
  `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`,
  `SECURITY_TOKEN_JWT_API`, `SECURITY_TOKEN_CREATE_USER_API`, `SECURITY_CORS_ORIGINS`
- Precisa de um PostgreSQL rodando.  <!-- TODO: docker? local? -->

## Arquitetura da API
- `domain/<entidade>/` → `dto`, `entity`, `mapper`, `repository`, `service`
- `infra/` → preocupações transversais (`authentication`, `exception`)
- Exceções customizadas ficam em `infra/exception/exceptions`

## Convenções
- Migrations Flyway em `src/main/resources/db/migration`, formato `V<n>__descricao.sql`.
  Nunca editar uma migration já aplicada: criar uma nova.
- Commits em português, no estilo Conventional Commits (`feat:`, `fix:`...)
- Branches: `feature/<descricao>`
