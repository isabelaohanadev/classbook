# ClassBook — Biblioteca Escolar Digital

[![CI](https://github.com/isabelaohanadev/classbook/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/isabelaohanadev/classbook/actions/workflows/ci.yml)

Biblioteca Escolar Digital desenvolvida em Java e Spring Boot para projeto acadêmico.

O ClassBook permite cadastrar, editar, listar, consultar e excluir **livros, autores, categorias, disciplinas e materiais digitais** de uma escola de ensino médio, com pesquisa avançada de livros. O backend expõe uma API REST; a interface desktop (JavaFX) consome essa API.

> **Status:** em planejamento. O backlog completo está nas [Issues](../../issues) e no GitHub Project **ClassBook — Planejamento e Desenvolvimento**. Nenhum CRUD foi implementado ainda.

## Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Backend | Spring Boot 4.1.x, API REST |
| Persistência | Spring Data JPA, PostgreSQL 16+ |
| Migrations | Flyway |
| Validação | Bean Validation |
| Documentação da API | Swagger/OpenAPI (a configurar — Issue #47) |
| Interface | JavaFX, consumindo a API REST (a criar — Issue #54) |
| Build | Maven (wrapper incluso) |
| CI | GitHub Actions |

## Estrutura do repositório

```text
.
├── .github/workflows/ci.yml      # CI: mvn -B verify em PRs e na main
├── docs/                         # requisitos, modelagem, plano de trabalho, contribuições
├── desktop/                      # interface JavaFX (estrutura a definir — Issue #54)
├── src/main/java/br/com/classbook/
│   ├── model/                    # entidades JPA
│   ├── dto/                      # DTOs de entrada e saída
│   ├── repository/               # Spring Data JPA
│   ├── service/                  # regras de negócio
│   ├── controller/               # rotas REST
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

1. Crie o usuário e os bancos (ajuste a senha):

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

2. Copie o modelo de variáveis de ambiente e ajuste a senha:

   ```bash
   cp .env.example .env
   ```

   O arquivo `.env` é lido automaticamente pela aplicação e **não é versionado** (está no `.gitignore`). Variáveis de ambiente do sistema têm prioridade sobre o `.env`.

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/classbook` | URL JDBC do banco |
| `DB_USERNAME` | `classbook` | Usuário do banco |
| `DB_PASSWORD` | *(sem padrão)* | Senha do banco. Sem ela a aplicação não inicia |

## Como executar

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Na inicialização, o Flyway aplica as migrations de `src/main/resources/db/migration/`.

## Como testar

Os testes de integração usam o PostgreSQL. Para não misturar com seus dados de desenvolvimento, aponte para o banco `classbook_test`:

```bash
DB_URL=jdbc:postgresql://localhost:5432/classbook_test ./mvnw verify
```

É o mesmo comando que o CI executa (`mvn -B verify`), contra um PostgreSQL temporário criado pelo próprio workflow.

## Documentação

- [Requisitos](docs/REQUISITOS.md)
- [Modelagem](docs/MODELAGEM.md)
- [Plano de trabalho](docs/PLANO_DE_TRABALHO.md)
- [Contribuições](docs/CONTRIBUICOES.md)
- [Como contribuir (GitHub Flow)](CONTRIBUTING.md)

## Equipe

| Integrante | GitHub | Responsabilidades principais |
| --- | --- | --- |
| Isabela Ohana | [@isabelaohanadev](https://github.com/isabelaohanadev) | CRUD de Autor e Livro, relacionamento Livro–Autor, pesquisa avançada de livros, testes dessas rotas, tela de catálogo e navegação (JavaFX) |
| Juan Ribeiro | [@JuanRiber](https://github.com/JuanRiber) | CRUD de Categoria, Disciplina e MaterialDigital, relacionamento Material–Disciplina, testes dessas rotas e validações, formulários de cadastro e edição (JavaFX) |

Responsabilidades compartilhadas: requisitos e modelagem, configuração inicial, arquitetura, validações e tratamento de erros, revisão de código, Swagger, README e apresentação.

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
