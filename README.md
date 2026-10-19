# MOTIVA — Sprint 4 (Spring Boot + Spring Data JPA + API REST)

RM: 563556, 565303, 563477, 564066

O motor de priorização de manutenção das Sprints 1–3 agora é uma **API REST**. Ela roda em Spring Boot 4.1 e usa Spring Data JPA sobre as **mesmas tabelas `T_MOT_*` do Oracle FIAP** da Sprint 3.

```
Sprint 3: Main.java → GeradorRelatorio → 4 DAOs (486 linhas de JDBC) → ConexaoBD → Oracle
Sprint 4: HTTP/JSON → @RestController → @Service → JpaRepository (20 linhas úteis) → Hikari → Oracle
```

## Stack

| Item | Versão / escolha |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.1 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`) |
| Banco | Oracle FIAP — `oracle.fiap.com.br:1521:ORCL` |
| Driver | `com.oracle.database.jdbc:ojdbc17` via Maven (o `lib/ojdbc17.jar` manual morreu) |
| Testes | `@SpringBootTest` + MockMvc + H2 em memória (rodam sem a rede da FIAP) |

## Estrutura

```
src/main/java/br/com/motiva/
├── MotivaApplication.java        @SpringBootApplication
├── model/                        @Entity: TrechoRodovia, EquipeManutencao, IntervencaoOperacional, RelatorioPrioridade
│                                 + enums NivelPrioridade, StatusIntervencao e SimNaoConverter (CHAR 'S'/'N' ↔ Boolean)
├── repository/                   interfaces JpaRepository com derived queries — zero SQL
├── service/                      @Service: MotorPrioridade, GeradorRelatorio, TrechoService, EquipeService, IntervencaoService
├── controller/                   @RestController: /api/trechos, /api/equipes, /api/intervencoes, /api/relatorios
├── dto/                          records de entrada (com Bean Validation) e saída — a entity nunca vai direto pro JSON
└── exception/                    GlobalExceptionHandler (@RestControllerAdvice) + exceções de negócio
sql/
├── script-criacao.sql            tabelas da Sprint 3 + SEQUENCES SEQ_MOT_* (ver "Mudança no banco")
└── script-dados.sql              mesmos dados de teste da Sprint 3
```

Cada camada só conversa com a vizinha: **Controller → Service → Repository → Banco**.

## Como executar

### 1. Banco (uma vez)

No SQL Developer, conectado com o seu RM, rode com F5 o `sql/script-criacao.sql` e depois o `sql/script-dados.sql`.

> **Mudança no banco:** na Sprint 3 os IDs eram `GENERATED ALWAYS AS IDENTITY`. A Sprint 4 exige `@SequenceGenerator`, e o JPA precisa **enviar** o ID no INSERT. Uma coluna `GENERATED ALWAYS` recusa isso com ORA-32795. Por isso o script cria as sequences `SEQ_MOT_EQUIPE`, `SEQ_MOT_TRECHO`, `SEQ_MOT_INTERVENCAO` e `SEQ_MOT_RELATORIO`, e as colunas passam a ser `DEFAULT SEQ_MOT_x.NEXTVAL`. Nomes de tabelas, colunas e constraints não mudaram.

### 2. Credenciais (nunca no código)

A aplicação lê `DB_USER`, `DB_PASSWORD` e, opcionalmente, `DB_URL` de variáveis de ambiente. Também lê de um arquivo `.env` na raiz do projeto, que está no `.gitignore`. Use o `.env.example` como modelo.

```bash
# Linux / macOS / Git Bash
export DB_USER=rmXXXXXX
export DB_PASSWORD=sua_senha
```
```powershell
# Windows PowerShell
$env:DB_USER="rmXXXXXX"; $env:DB_PASSWORD="sua_senha"
```
Na IDE: Run Configuration → Environment variables.

### 3. Subir a API

```bash
mvn spring-boot:run
```
A API sobe em `http://localhost:8080`. O console mostra o SQL que o Hibernate gera (`spring.jpa.show-sql=true`).
Sem Maven instalado: abra a pasta no IntelliJ, que usa o Maven embutido, e rode a `MotivaApplication` com as variáveis de ambiente configuradas.

Com os dados do `script-dados.sql`, o primeiro `POST /api/relatorios` deve devolver **URGENTE 2, CRITICO 1, ATENCAO 1, NORMAL 2** (total 6). É a mesma classificação da Sprint 3.

### 4. Testes

```bash
mvn test
```
São 8 testes de API e 2 unitários do motor de prioridade, todos em H2. Não precisam do Oracle.

## Endpoints

### Trechos — `/api/trechos`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/trechos` | Lista todos (filtros opcionais `?rodovia=SP-348` e `?prioridade=URGENTE`) | 200 | 400 (prioridade inválida) |
| GET | `/api/trechos/{id}` | Busca por ID | 200 | 404 |
| GET | `/api/trechos/desgaste?minimo=60` | Trechos com desgaste ≥ mínimo (derived query) | 200 | 400 |
| POST | `/api/trechos` | Cria | 201 + `Location` | 400 |
| PUT | `/api/trechos/{id}` | Atualiza | 200 | 400 / 404 |
| DELETE | `/api/trechos/{id}` | Remove | 204 | 404 / 409 (tem intervenção) |

### Equipes — `/api/equipes`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/equipes` | Lista (filtros `?ativa=true` e `?especialidade=Drenagem`) | 200 | — |
| GET | `/api/equipes/{id}` | Busca por ID | 200 | 404 |
| POST | `/api/equipes` | Cria (`ativa` padrão = true) | 201 | 400 |
| PUT | `/api/equipes/{id}` | Atualiza | 200 | 400 / 404 |
| DELETE | `/api/equipes/{id}` | Remove | 204 | 404 / 409 (tem intervenção) |

### Intervenções — `/api/intervencoes`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/intervencoes` | Lista (filtros `?tipo=`, `?status=PLANEJADA`, `?idTrecho=1`) | 200 | 400 |
| GET | `/api/intervencoes/{id}` | Busca por ID | 200 | 404 |
| POST | `/api/intervencoes` | Cria | 201 | 400 (trecho/equipe inexistente, equipe inativa, datas) |
| PUT | `/api/intervencoes/{id}` | Atualiza | 200 | 400 / 404 |
| DELETE | `/api/intervencoes/{id}` | Remove | 204 | 404 |

### Relatórios de prioridade — `/api/relatorios`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/relatorios` | Classifica os trechos atuais, grava no histórico e devolve o relatório + trechos ordenados por severidade | 201 | — |
| GET | `/api/relatorios` | Histórico, do mais recente para o mais antigo | 200 | — |
| GET | `/api/relatorios/periodo?inicio=2026-10-01&fim=2026-10-31` | Histórico no período (datas ISO, fim inclusivo) | 200 | 400 (inicio > fim ou data inválida) |
| GET | `/api/relatorios/{id}` | Um relatório | 200 | 404 |

### Regra de prioridade (`service/MotorPrioridade`)

| Prioridade | Condição |
|---|---|
| URGENTE | desgaste ≥ 80 **ou** acidentes/ano ≥ 15 |
| CRITICO | desgaste ≥ 60 **ou** acidentes/ano ≥ 10 |
| ATENCAO | desgaste ≥ 40 **ou** acidentes/ano ≥ 5 |
| NORMAL | demais casos |

### Formato de erro (todos os 4xx/5xx)

```json
{
  "timestamp": "2026-10-20T19:42:10.123",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "caminho": "/api/trechos",
  "campos": { "codigoRodovia": "codigoRodovia é obrigatório", "indiceDesgaste": "indiceDesgaste deve ser <= 100" }
}
```

## Exemplos cURL

> No Windows PowerShell, `curl` é um apelido do `Invoke-WebRequest`. Use `curl.exe`, o Git Bash ou o Postman.

```bash
# 1. Listar trechos (200)
curl -i http://localhost:8080/api/trechos

# 2. Buscar por ID (200) e um ID inexistente (404)
curl -i http://localhost:8080/api/trechos/1
curl -i http://localhost:8080/api/trechos/9999

# 3. Criar trecho (201 + Location)
curl -i -X POST http://localhost:8080/api/trechos \
  -H "Content-Type: application/json" \
  -d '{"codigoRodovia":"SP-021","kmInicial":5.0,"kmFinal":12.5,"indiceDesgaste":72.0,"acidentesAno":4,"fluxoDiario":110000}'

# 4. Criar trecho inválido (400 com lista de campos)
curl -i -X POST http://localhost:8080/api/trechos \
  -H "Content-Type: application/json" \
  -d '{"codigoRodovia":"","kmInicial":10,"kmFinal":5,"indiceDesgaste":150,"acidentesAno":0,"fluxoDiario":1}'

# 5. Atualizar trecho (200) — a prioridade é recalculada
curl -i -X PUT http://localhost:8080/api/trechos/1 \
  -H "Content-Type: application/json" \
  -d '{"codigoRodovia":"SP-348","kmInicial":10.0,"kmFinal":25.5,"indiceDesgaste":30.0,"acidentesAno":2,"fluxoDiario":95000}'

# 6. Remover trecho com intervenção (409) e sem intervenção (204)
curl -i -X DELETE http://localhost:8080/api/trechos/1
curl -i -X DELETE http://localhost:8080/api/trechos/6

# 7. Derived query: trechos com desgaste >= 60
curl -i "http://localhost:8080/api/trechos/desgaste?minimo=60"

# 8. Criar equipe (201) e listar só as ativas
curl -i -X POST http://localhost:8080/api/equipes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Equipe Epsilon","responsavel":"Marina Alves","qtMembros":6,"especialidade":"Roçada"}'
curl -i "http://localhost:8080/api/equipes?ativa=true"

# 9. Criar intervenção (201) e tentar usar a Equipe Delta, que está inativa (400)
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H "Content-Type: application/json" \
  -d '{"idTrecho":4,"idEquipe":1,"tipo":"Roçada mecanizada","dataInicio":"2026-10-20","status":"PLANEJADA"}'
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H "Content-Type: application/json" \
  -d '{"idTrecho":4,"idEquipe":4,"tipo":"Roçada mecanizada","dataInicio":"2026-10-20","status":"PLANEJADA"}'

# 10. findByTipo
curl -i "http://localhost:8080/api/intervencoes?tipo=Limpeza%20de%20drenagem"

# 11. Gerar relatório (201), histórico (200) e período (200)
curl -i -X POST http://localhost:8080/api/relatorios
curl -i http://localhost:8080/api/relatorios
curl -i "http://localhost:8080/api/relatorios/periodo?inicio=2026-10-01&fim=2026-12-31"
```

## Evidências

Os prints das requisições estão na pasta `evidencias/`. Cobrem CRUD com 200/201/204/404/400/409 e os três endpoints de relatório.

## Perguntas de reflexão

**1. Por que o Repository é uma interface e não uma classe? Quem escreve a implementação e quando?**
Na interface a gente só declara *o que* quer: salvar, buscar por ID, buscar por tipo. A implementação é escrita pelo Spring Data JPA em **tempo de execução, na subida da aplicação**. Ele cria um proxy que delega para o `SimpleJpaRepository` (onde moram `save`, `findById`, `deleteById`…) e gera o código das derived queries. Depois injeta esse proxy no construtor dos nossos `@Service`. Por ser interface, o contrato fica desacoplado da tecnologia, e o framework pode trocar ou otimizar a implementação sem a gente reescrever nada.

**2. O pattern DAO da Sprint 3 "morreu" na migração ou só mudou de forma?**
Só mudou de forma. A responsabilidade continua a mesma: isolar o acesso a dados do resto do sistema, e o `@Service` continua sem saber como o banco funciona. O que acabou foi o trabalho braçal. Abrir `Connection`, montar `PreparedStatement`, mapear `ResultSet` campo a campo, fechar recurso e tratar `SQLException` agora é feito pelo Hibernate, pelo pool Hikari e pelo `@Transactional`. Nossos 4 DAOs tinham 486 linhas e viraram 4 interfaces com cerca de 20 linhas úteis. O Repository **é** um DAO, só que gerado.

**3. Por que a validação `nivelVegetacao >= 0` (no nosso caso, `indiceDesgaste` entre 0 e 100 e `kmFinal > kmInicial`) deve ficar no Service e não no Controller?**
O Controller é só a porta HTTP. O Service é o único caminho obrigatório para qualquer operação de negócio. Se a regra estivesse no Controller, qualquer outro ponto de entrada passaria por fora dela: um job agendado, uma importação de planilha, outro service, um teste. Um trecho inválido entraria no banco e quebraria a priorização. No Service, a regra vale para todos os clientes e é testável sem HTTP. A gente também usa Bean Validation no DTO (`@DecimalMin`, `@DecimalMax`) para devolver o 400 rápido na fronteira. A **garantia** da regra, porém, está no `TrechoService.validarRegras`, que também cobre a regra entre campos (`kmFinal > kmInicial`), coisa que uma anotação de campo único não expressa.

**4. No JDBC puro vocês escreviam SQL. Onde está o SQL do `findByTipo()`? Quem o gerou?**
Não está em lugar nenhum do nosso código. Na subida, o Spring Data **lê o nome do método**: `find` + `By` + propriedade `tipo`. Com isso ele monta a consulta JPQL `select i from IntervencaoOperacional i where i.tipo = ?1`, já validando que a propriedade `tipo` existe na entity. Na execução, o Hibernate traduz essa consulta para o dialeto Oracle usando os nomes do `@Table`/`@Column`. O resultado é algo como `select ... from T_MOT_INTERVENCAO i1_0 where i1_0.DS_TIPO=?`. Dá para ver esse SQL no console, porque `spring.jpa.show-sql=true`.

## Boas práticas de Git adotadas

- Commits pequenos, um por camada ou funcionalidade.
- `.gitignore` cobre `target/`, arquivos de IDE e `.env`.
- Nenhuma senha no repositório: credenciais só por variável de ambiente ou `.env` local.
- Driver via Maven em vez de JAR versionado no repositório.
