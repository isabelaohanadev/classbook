# ClassBook — Biblioteca Escolar Digital

[![CI](https://github.com/isabelaohanadev/classbook/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/isabelaohanadev/classbook/actions/workflows/ci.yml)

Biblioteca Escolar Digital desenvolvida em Java e Spring Boot para projeto acadêmico.

O ClassBook permite cadastrar, editar, listar, consultar e excluir **livros, autores, categorias, disciplinas e materiais digitais** de uma escola de ensino médio, com pesquisa avançada de livros. O backend expõe uma API REST; a interface desktop (JavaFX) consome essa API e é opcional, construída depois dos itens avaliados (decisão da Issue #93).

> **Status:** backend concluído. Os cinco CRUDs, os relacionamentos, a pesquisa avançada de livros, as validações, o tratamento padronizado de erros e a documentação Swagger estão implementados e testados, e o CI está verde na `main`. A interface desktop (opcional) já tem todas as telas implementadas: cliente HTTP, navegação, catálogo com busca e filtros, cadastro e edição dos cinco recursos, mensagens de sucesso e erro, e detalhes do livro com os materiais digitais (ver `desktop/README.md`). Falta o teste com o backend integrado (Issue #62). O andamento está nas [Issues](../../issues) e no GitHub Project **ClassBook — Planejamento e Desenvolvimento**.

## Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Backend | Spring Boot 4.1.x, API REST |
| Persistência | Spring Data JPA, PostgreSQL 16+ |
| Migrations | Flyway |
| Validação | Bean Validation |
| Documentação da API | Swagger/OpenAPI (springdoc), em `/swagger-ui.html` |
| Interface | JavaFX 21, consumindo a API REST — opcional, pós-avaliação (decisão da Issue #93), em `desktop/` |
| Build | Maven (wrapper incluso) |
| CI | GitHub Actions |

## Estrutura do repositório

```text
.
├── .github/workflows/ci.yml      # CI: mvn -B verify em PRs e na main
├── docs/                         # requisitos, modelagem, arquitetura, plano de trabalho, contribuições
├── desktop/                      # interface JavaFX: projeto Maven separado (ver desktop/README.md)
├── src/main/java/br/com/classbook/
│   ├── model/                    # entidades JPA
│   ├── dto/                      # records de entrada (Request) e de saída (Response)
│   ├── repository/               # Spring Data JPA
│   ├── service/                  # regras de negócio
│   ├── controller/               # rotas REST (/api/...)
│   ├── exception/                # exceções e tratamento global de erros
│   └── config/                   # configurações (Swagger etc.)
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/             # migrations Flyway
├── src/test/java/br/com/classbook/
├── CONTRIBUTING.md               # GitHub Flow, branches, commits e PRs
├── pom.xml
└── .env.example                  # modelo das variáveis de ambiente
```

## Pré-requisitos

- **JDK 21** (compatível com versões mais novas do JDK; o projeto compila para Java 21).
- **PostgreSQL 16 ou superior** em execução local.
- Maven **não precisa estar instalado**: use o wrapper (`./mvnw`, ou `mvnw.cmd` no Windows).

## Como compilar

Para só compilar e empacotar, sem banco de dados e sem rodar os testes, basta o JDK:

```bash
./mvnw -B -DskipTests verify
```

No Windows, use `mvnw.cmd`. Na primeira execução o wrapper baixa o Maven e as dependências, o que pode levar alguns minutos. Para executar a aplicação ou rodar os testes é preciso configurar o banco, como descrito a seguir.

## Configuração do banco

1. Conectado ao PostgreSQL como superusuário (por exemplo, `psql -U postgres`), crie o usuário e os bancos (ajuste a senha):

   ```sql
   CREATE USER classbook WITH PASSWORD 'defina_uma_senha_local';
   CREATE DATABASE classbook OWNER classbook;
   CREATE DATABASE classbook_test OWNER classbook;
   ```

   *Alternativa opcional, se você já usa Docker (não é obrigatório para o projeto):*

   ```bash
   docker run -d --name classbook-db -e POSTGRES_DB=classbook -e POSTGRES_USER=classbook \
     -e POSTGRES_PASSWORD=defina_uma_senha_local -p 5432:5432 postgres:16
   ```

   Esse comando cria só o banco `classbook`. Quando o contêiner terminar de iniciar, crie também o banco dos testes:

   ```bash
   docker exec classbook-db createdb -U classbook classbook_test
   ```

2. Copie o modelo de variáveis de ambiente e ajuste a senha:

   ```bash
   cp .env.example .env
   ```

   O arquivo `.env` é lido automaticamente pela aplicação e **não é versionado** (está no `.gitignore`). Variáveis de ambiente do sistema têm prioridade sobre o `.env`. Ele é procurado na pasta de onde o Maven é executado, então rode os comandos a partir da raiz do projeto. O `.env` usa o formato de arquivo `.properties`: não use a barra invertida (`\`) na senha.

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/classbook` | URL JDBC do banco |
| `DB_USERNAME` | `classbook` | Usuário do banco |
| `DB_PASSWORD` | *(sem padrão)* | Senha do usuário do banco. Precisa ser a mesma definida no passo 1 |

Se `DB_PASSWORD` não for definida ou estiver errada, a conexão falha com `password authentication failed for user "classbook"`. A exceção é um PostgreSQL configurado com autenticação `trust`, que aceita a conexão mesmo sem senha.

## Como executar

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Na inicialização, o Flyway aplica as migrations de `src/main/resources/db/migration/`.

Com a aplicação no ar, a documentação das rotas fica disponível no navegador:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`, para ver e testar cada rota
- **OpenAPI (JSON):** `http://localhost:8080/v3/api-docs`

### Rotas da API

Cada recurso tem as seis operações: `POST` (cadastra e responde `201` com `Location`), `GET` (lista), `GET /{id}` (consulta), `PUT /{id}` (edita, substituindo o registro inteiro), `PATCH /{id}` (edita só os campos enviados, no formato JSON Merge Patch: campo ausente não muda e `null` limpa um campo opcional) e `DELETE /{id}` (exclui e responde `204`).

| Recurso | Rota | Observações |
| --- | --- | --- |
| Autores | `/api/autores` | Um autor com livros não pode ser excluído (`409`). |
| Livros | `/api/livros` | Pesquisa avançada no `GET`: `?busca=` (texto livre: título, autor, categoria ou ano), `?titulo=`, `?autorId=` e `?categoriaId=`, opcionais e combináveis. O ISBN é único (`409`), e a categoria e a disciplina são opcionais. |
| Categorias | `/api/categorias` | Nome único, sem diferenciar maiúsculas (`409`). Uma categoria com livros não pode ser excluída (`409`). |
| Disciplinas | `/api/disciplinas` | Código único, gravado em maiúsculas (`409`). Uma disciplina com materiais não pode ser excluída (`409`). |
| Materiais digitais | `/api/materiais` | Cada material pertence a uma disciplina (`disciplinaId`), e a URL precisa ser `http` ou `https`. |

### Interface desktop (opcional)

A interface JavaFX é um projeto Maven separado, em `desktop/`, que só conversa com a API. Com o backend no ar:

```bash
cd desktop
./mvnw javafx:run
```

Para usar outro endereço da API, defina `CLASSBOOK_API_URL` (o padrão é `http://localhost:8080/api`). Os detalhes, o que já está pronto e como rodar os testes da interface estão no [README do desktop](desktop/README.md).

## Como testar

Os testes de integração usam o PostgreSQL. Para não misturar com seus dados de desenvolvimento, aponte para o banco `classbook_test`:

```bash
DB_URL=jdbc:postgresql://localhost:5432/classbook_test ./mvnw verify
```

É o mesmo comando que o CI executa (`mvn -B verify`), contra um PostgreSQL temporário criado pelo próprio workflow.

### Migrations em um banco vazio

O CI começa sempre com um banco vazio: a cada execução, o Flyway cria as tabelas do zero, e o `MigrationsTests` confere que todas as migrations foram aplicadas sem erro. Para repetir isso localmente, por exemplo depois de criar uma migration:

1. Crie um banco novo, conectado como superusuário (`psql -U postgres`), ou com o Docker:

   ```sql
   CREATE DATABASE classbook_vazio OWNER classbook;
   ```

   ```bash
   docker exec classbook-db createdb -U classbook classbook_vazio
   ```

2. Suba a aplicação apontando para ele:

   ```bash
   DB_URL=jdbc:postgresql://localhost:5432/classbook_vazio ./mvnw spring-boot:run
   ```

   O log deve mostrar `Successfully applied N migrations` e depois `Started ClassbookApplication`. Se uma entidade não bater com as tabelas, a aplicação não sobe: o `ddl-auto=validate` falha com `Schema-validation` e o nome da coluna ou da tabela.

3. Em outro terminal, confira o histórico do Flyway. Cada migration de `src/main/resources/db/migration/` aparece uma vez, com `success` igual a `t`:

   ```bash
   psql -U classbook -d classbook_vazio -c "SELECT version, script, success FROM flyway_schema_history ORDER BY installed_rank;"
   ```

4. Pare a aplicação e apague o banco (`DROP DATABASE classbook_vazio;` como superusuário, ou `docker exec classbook-db dropdb -U classbook classbook_vazio`).

## Erros da API

Todo erro sai no mesmo formato JSON (`horario`, `status`, `erro`, `mensagem`, `caminho` e `campos`), com mensagem em português; o detalhe está em [Arquitetura, Erros e exceções](docs/ARQUITETURA.md#erros-e-exceções).

| Código | Quando |
| --- | --- |
| `400` | Corpo ou parâmetro inválido: campo obrigatório ausente, valor fora da regra, enum fora da lista, JSON malformado, ID que não é número, ou ID de autor, categoria ou disciplina inexistente no corpo. `campos` lista o que está errado. |
| `404` | O `{id}` da rota não existe, ou a rota não existe. |
| `405` / `415` | Método HTTP não suportado na rota / corpo que não é JSON. |
| `409` | Duplicidade (ISBN, nome de categoria, código de disciplina) ou exclusão de um registro ainda referenciado. |
| `500` | Erro inesperado, com mensagem genérica (o detalhe vai só para o log). |

## Documentação

- [Requisitos](docs/REQUISITOS.md)
- [Modelagem](docs/MODELAGEM.md)
- [Arquitetura e convenções do backend](docs/ARQUITETURA.md)
- [Plano de trabalho](docs/PLANO_DE_TRABALHO.md)
- [Contribuições](docs/CONTRIBUICOES.md)
- [Roteiro da apresentação](docs/APRESENTACAO.md)
- [Como contribuir (GitHub Flow)](CONTRIBUTING.md)

## Equipe

| Integrante | GitHub | Responsabilidades principais |
| --- | --- | --- |
| Isabela Ohana | [@isabelaohanadev](https://github.com/isabelaohanadev) | CRUD de Autor e Livro, relacionamento Livro–Autor, pesquisa avançada de livros, testes dessas rotas, tela de catálogo e navegação (JavaFX) |
| Juan Ribeiro | [@JuanRiber](https://github.com/JuanRiber) | CRUD de Categoria, Disciplina e MaterialDigital, relacionamento Material–Disciplina, testes dessas rotas e validações, formulários de cadastro e edição (JavaFX) |

Responsabilidades compartilhadas: requisitos e modelagem, configuração inicial, arquitetura, validações e tratamento de erros, revisão de código, Swagger, README e apresentação.

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
