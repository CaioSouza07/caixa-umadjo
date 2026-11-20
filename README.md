# caixa-umadjo

Monorepo com a API (Spring Boot) e o front web (React) do caixa-umadjo.

## Stack

- **API** (`apps/api`): Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, Flyway, PostgreSQL, Maven
- **Web** (`apps/web`): React 19, Vite, JavaScript (JSX), ESLint
- **Infra**: Docker Compose (PostgreSQL 17, e opcionalmente API e web em containers)

## Estrutura

```
apps/
  api/        API Spring Boot
  web/        Front React
infra/        Arquivos do Docker Compose
scripts/      Atalhos para subir/parar o ambiente (ver scripts/README.md)
.env.example  Modelo das variáveis de ambiente
```

## Pré-requisitos

- Java 21 e Maven 3.9+
- Node.js 20.19+ ou 22.12+
- Docker com Compose (no Windows, pelo WSL)

## Configuração

Crie o `.env` na raiz a partir do modelo e preencha os valores em branco:

```bash
cp .env.example .env
```

| Variável | Descrição |
|---|---|
| `DATASOURCE_URL` | URL JDBC do banco |
| `DATASOURCE_USERNAME` | Usuário do banco (o container do Postgres é criado com ele) |
| `DATASOURCE_PASSWORD` | Senha do banco |
| `SECURITY_TOKEN_JWT_API` | Segredo para assinar os tokens JWT. Gere um com `openssl rand -base64 48` |
| `SECURITY_CORS_ORIGINS` | Origens liberadas para chamar a API, separadas por vírgula |
| `POSTGRES_DB` | Nome do banco criado no container |
| `POSTGRES_PORT` | Porta do banco na máquina |

O `.env` não vai para o Git.

## Rodando em desenvolvimento

Banco no Docker, API e front rodando na máquina:

```bash
./scripts/db-up.sh

cd apps/api && mvn spring-boot:run

cd apps/web && npm install && npm run dev
```

Na primeira vez, com a API já rodando (o Flyway cria as tabelas ao subir), crie o usuário admin:

```bash
./scripts/db-create-admin.sh <senha>
```

Login: `admin@email.com` com a senha escolhida.

| Serviço | Endereço |
|---|---|
| Front | http://localhost:5173 |
| API | http://localhost:8080 |
| Health da API | http://localhost:8080/actuator/health |
| Banco | `localhost:5432` |

## Rodando tudo no Docker

```bash
./scripts/up.sh     # banco, API e front
./scripts/down.sh   # para API e front
```

Nesse modo o front fica em http://localhost:3000. Os demais scripts (logs, status, reset do banco etc.) estão em [scripts/README.md](scripts/README.md).

## Testes e verificações

```bash
cd apps/api && mvn test          # não precisa de banco nem de .env
cd apps/web && npm run lint
cd apps/web && npm run build
```

## CI

O workflow `.github/workflows/ci.yml` roda a cada PR (abertura e cada push), a cada push na `main` e manualmente pela aba Actions. Os jobs rodam em paralelo:

| Job | O que verifica |
|---|---|
| API (build + testes) | `mvn verify`: compila, roda os testes e gera o `.jar` |
| Web (lint + build) | `npm ci`, `npm run lint` e `npm run build` |
| Scripts (shellcheck) | Erros comuns de bash em `scripts/*.sh` |
| Smoke test (Docker) | Sobe banco, API e front com `./scripts/up.sh` e confere se a API responde `UP` (o que inclui conexão com o banco e migrations aplicadas) e se o front responde |

O smoke test não testa funcionalidades, só se a aplicação sobe e responde. Funcionalidades são cobertas pelos testes da API.

## Convenções

- Branches: `feature/<descricao>`
- Commits: Conventional Commits (`feat:`, `fix:`, `chore:`...), de preferência em inglês
- Migrations do Flyway em `apps/api/src/main/resources/db/migration`, no formato `V<n>__descricao.sql`. Uma migration já aplicada nunca é editada: crie uma nova.
