# Scripts

Atalhos para subir, parar e inspecionar o ambiente Docker do projeto (banco, API e front).
Por baixo, todos chamam o `docker compose` com `infra/docker-compose.yml` + `infra/docker-compose.app.yml` e o `.env` da raiz.

## Antes de usar

- Docker com Compose instalado (no Windows, rodar os scripts pelo **WSL**).
- `.env` na raiz do repo, criado a partir do `.env.example`.

Os scripts funcionam de qualquer pasta:

```bash
./scripts/up.sh
# ou, se der "Permission denied":
bash scripts/up.sh
```

## Resumo

| Script | O que faz |
|---|---|
| `up.sh` | Sobe API e front (e o banco, se estiver parado) |
| `down.sh` | Para API e front; o banco continua rodando |
| `db-up.sh` | Sobe só o banco |
| `db-down.sh` | Para tudo, **mantendo** os dados do banco |
| `db-purge.sh` | Para tudo e **apaga** os dados do banco |
| `logs.sh` | Mostra os logs em tempo real |
| `status.sh` | Mostra o que está rodando e se está saudável |
| `db-psql.sh` | Abre o terminal SQL (`psql`) dentro do banco |
| `db-create-admin.sh` | Cria (ou atualiza) o usuário admin |
| `clean.sh` | Limpa sobras de build do Docker |

## Detalhes

### `up.sh`
Faz o build das imagens da API e do front e sobe os containers. Espera tudo ficar saudável antes de terminar e mostra os endereços:

- Front: http://localhost:3000
- API: http://localhost:8080
- Health da API: http://localhost:8080/actuator/health

O build sempre roda, mas quando o código não mudou o Docker reaproveita o cache e leva poucos segundos.
Se algo falhar, veja `./scripts/logs.sh api`.

### `down.sh`
Para e remove os containers da API e do front. O banco continua no ar, útil para rodar a API ou o front localmente (`mvn spring-boot:run` / `npm run dev`) usando o banco do Docker.

### `db-up.sh`
Sobe só o Postgres e espera ele ficar pronto. É o uso mais comum no dia a dia: banco no Docker, API e front rodando na máquina.

### `db-down.sh`
Para o banco **sem apagar os dados**: eles ficam guardados num volume do Docker e voltam no próximo `db-up.sh`.
A API e o front também param, se estiverem rodando, porque não funcionam sem o banco.

### `db-purge.sh`
Para tudo e **apaga o volume do banco**: todas as tabelas e dados somem. No próximo `db-up.sh` / `up.sh` o Flyway recria as tabelas do zero.
Pede confirmação; para pular, use `-y`:

```bash
./scripts/db-purge.sh -y && ./scripts/db-up.sh   # reset completo do banco
```

Depois do reset não existe nenhum usuário: suba a API e crie o admin com `db-create-admin.sh`.

### `logs.sh`
Mostra as últimas 100 linhas e continua acompanhando. `Ctrl+C` sai (os containers continuam rodando).

```bash
./scripts/logs.sh              # todos
./scripts/logs.sh api          # só a API
./scripts/logs.sh api web      # API e front
```

O que aparece em cada um:

- **api**: subida do Spring, migrations do Flyway, erros e stack traces, e qualquer `log.info`/`log.error` do código.
- **web**: logs do nginx, uma linha por requisição (`"GET /assets/index.js" 200`) e erros dele. `console.log` e erros do React **não** aparecem aqui: rodam no navegador, veja no DevTools (F12 → Console).
- **postgres**: subida, conexões recusadas, senha errada, erros de SQL. Consultas normais não aparecem.

### `status.sh`
Lista os containers do projeto, incluindo os que pararam ou caíram:

```
SERVICE    STATUS                    PORTS
api        Up 2 minutes (healthy)    0.0.0.0:8080->8080/tcp
postgres   Up 3 minutes (healthy)    0.0.0.0:5432->5432/tcp
web        Up 1 minute               0.0.0.0:3000->80/tcp
```

`(healthy)` = respondendo; `(health: starting)` = ainda subindo; `Exited (1)` = caiu, veja os logs.

### `db-psql.sh`
Abre o `psql` já conectado ao banco do projeto, sem pedir senha. O banco precisa estar rodando.

```bash
./scripts/db-psql.sh                                      # interativo: \dt lista tabelas, \q sai
./scripts/db-psql.sh -c "select email, role from users"   # um comando só
```

### `db-create-admin.sh`
Cria o usuário admin direto no banco, com a senha passada como argumento:

```bash
./scripts/db-create-admin.sh minhaSenha123
```

- Nome `admin`, e-mail `admin@email.com`, papel `ADMIN`.
- A senha segue as mesmas regras da API: mínimo de 6 caracteres e máximo de 72 bytes.
- A senha é salva com hash BCrypt gerado pelo próprio Postgres (extensão `pgcrypto`, removida no fim se não existia antes), no mesmo formato que a API usa no login.
- Se o admin já existir, a senha é trocada e ele volta a ser `ADMIN` e ativo. Serve também para recuperar a senha.
- Precisa do banco rodando e das tabelas criadas: a API tem que ter subido ao menos uma vez (o Flyway cria as tabelas). Depois de um `db-purge.sh`, suba a API de novo antes de rodar este script.
- A senha fica no histórico do terminal. Para evitar, comece o comando com um espaço (` ./scripts/db-create-admin.sh ...`), o bash não grava comandos que começam com espaço.

### `clean.sh`
Remove imagens `<none>` (versões antigas que sobram a cada build com código alterado) e cache de build sem uso, mostrando o espaço antes e depois.
Vale para o Docker inteiro, mas só apaga o que **não** está em uso: não mexe em containers, imagens ativas nem volumes (os dados dos bancos ficam).

## Problemas comuns

| Erro | Causa | Solução |
|---|---|---|
| `Permission denied` | O arquivo não está marcado como executável | `bash scripts/<script>.sh`, ou `chmod +x scripts/*.sh` |
| `$'\r': command not found` | O script foi salvo com quebra de linha do Windows (CRLF) | O `.gitattributes` da raiz já força LF; se editou fora do Git, converta o arquivo para LF no editor |
| `port is already allocated` | Outra coisa usando a porta (ex.: Postgres instalado na máquina, ou a API rodando localmente na 8080) | Pare o outro processo, ou troque `POSTGRES_PORT` no `.env` |
| `up.sh` falha / API `unhealthy` | A API não conseguiu subir (variável faltando no `.env`, erro numa migration...) | `./scripts/logs.sh api` |

## Criando um script novo

1. Comece com estas duas linhas, para ganhar os caminhos, a função `compose` e a `env_value` (lê uma variável do `.env`):

   ```bash
   #!/usr/bin/env bash
   source "$(dirname "${BASH_SOURCE[0]}")/_common.sh"
   ```

2. Ao adicionar no Git, marque como executável:

   ```bash
   git add --chmod=+x scripts/novo-script.sh
   ```

   **Por quê:** no Linux/Mac um arquivo só roda com `./arquivo.sh` se tiver a permissão de execução, e o Git guarda essa marca junto com o arquivo. O Windows não tem essa marca, então sem esse comando o Git registra o script como arquivo comum, e quem clonar no Linux/Mac recebe `Permission denied`. No WSL dentro de `/mnt/c` não dá para perceber, porque ali tudo aparece como executável.
