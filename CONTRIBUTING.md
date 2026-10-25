# Como contribuir

O ClassBook segue o **GitHub Flow**: a branch `main` está sempre funcional e toda alteração entra por pull request (PR) revisado, com o CI verde.

## Fluxo de trabalho

1. **Mantenha a `main` funcional.** Ela só recebe código por PR.
2. **Crie uma branch curta para cada tarefa ou Issue**, a partir da `main` atualizada:

   ```bash
   git switch main && git pull
   git switch -c feature/crud-livro
   ```

3. **Implemente e faça commits com mensagens descritivas** (padrão abaixo).
4. **Abra um PR para `main`**, vinculando a Issue (`Closes #N` na descrição). O template já traz o checklist.
5. **Aguarde o CI terminar com sucesso** (aba *Checks* do PR).
6. **Solicite revisão do outro integrante.**
7. **Corrija o que for apontado** e integre o PR depois da aprovação.

**Não trabalhem diretamente na `main`** para implementar funcionalidades. Ao integrar, prefira *Squash and merge* e apague a branch.

## Nomes de branches

`tipo/descricao-curta-em-kebab-case`

| Exemplo | Quando usar |
| --- | --- |
| `feature/crud-autor`, `feature/crud-livro`, `feature/pesquisa-avancada` | Nova funcionalidade |
| `fix/validacao-isbn` | Correção de defeito |
| `docs/atualiza-readme` | Documentação |
| `test/rotas-de-livro` | Somente testes |
| `chore/ajusta-dependencias` | Manutenção, build, configuração |

## Mensagens de commit (Conventional Commits)

`tipo: descrição no imperativo e em letras minúsculas`

```text
feat: implementa CRUD de autores
feat: adiciona filtros ao catálogo
fix: corrige validação de ISBN duplicado
test: adiciona testes para livros
docs: documenta configuração do projeto
```

Tipos usados: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`. Os commits devem refletir alterações reais de cada integrante.

## Integração contínua (CI)

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) roda em **pull requests para `main`** e em **pushes na `main`**:

1. checkout do código;
2. Java 21 (Temurin) com cache do Maven;
3. `mvn -B verify` — compila, executa os testes e empacota. Qualquer falha reprova o job.

Os resultados dos testes aparecem no log do job, na aba **Actions**. Os testes de integração usam um PostgreSQL 16 temporário criado pelo workflow (serviço do GitHub Actions), sem segredos nem dependência do computador de ninguém.

Para reproduzir localmente o que o CI faz, veja *Como testar* no [README](README.md#como-testar).

## Revisão de código

- Todo PR é revisado pelo outro integrante antes de integrar.
- Revisar é ler o código, rodar quando fizer sentido e comentar. Aprove só o que você entenderia explicar na apresentação.
- Quem abriu o PR responde aos comentários e faz os ajustes na mesma branch.

## Proteção da branch `main`

Regras recomendadas, sem exagero para uma equipe de duas pessoas:

- exigir PR antes de integrar, com **1 aprovação**;
- exigir que a verificação **Build e testes** (job do CI) passe;
- bloquear *force push* e a exclusão da `main`.

Como configurar: **Settings → Branches → Add branch ruleset / protection rule** para `main`. Configurar essas regras exige permissão de administrador do repositório. O estado real de ativação é acompanhado na Issue #74.

## Java e dependências

- O projeto usa **Java 21**, definido pela propriedade `java.version` do `pom.xml`. O CI usa o mesmo Java 21 (Temurin).
- **As versões vêm do BOM do Spring Boot** (`spring-boot-starter-parent`). Não declare `<version>` em dependência que o BOM já gerencia (starters, driver do PostgreSQL, Flyway etc.) nem fixe versões por propriedade sem necessidade. Se for inevitável, explique o motivo na descrição do PR.
- **Mantenha o escopo simples** ([requisitos](docs/REQUISITOS.md)): toda dependência nova entra por PR, com o motivo na descrição.
- **Em PRs que alteram o `pom.xml`, confira que não há conflitos de versão:**

  ```bash
  ./mvnw -B dependency:tree -Dverbose | grep "omitted for conflict"
  ```

  Sem nenhuma linha na saída, não há conflitos. As linhas *version managed from* e *omitted for duplicate* são normais: o BOM alinha versões de dependências transitivas.

## Migrations (Flyway)

O schema do banco é criado e alterado **somente** por migrations Flyway, em `src/main/resources/db/migration/`. O Hibernate roda com `spring.jpa.hibernate.ddl-auto=validate`: confere se as entidades batem com as tabelas, mas não cria nem altera nada. Não troque para `update` ou `create` (o teste `MigrationInicialTests` falha se isso acontecer).

- **Migration inicial:** `V1__criar_tabelas_iniciais.sql` cria as cinco tabelas de [`docs/MODELAGEM.md`](docs/MODELAGEM.md).
- **Nunca edite uma migration já integrada na `main`.** O Flyway guarda o checksum de cada arquivo aplicado e, se o arquivo mudar, a aplicação não inicia (`Migration checksum mismatch`). Para corrigir ou evoluir o schema, crie uma migration nova.
- **Nome das próximas migrations:** `V<AAAAMMDDHHMM>__<descricao>.sql`, com a data e a hora (horário local) em que você cria o arquivo e uma descrição curta em `snake_case`, sem acentos, começando por um verbo. Exemplo: `V202611051430__adiciona_edicao_em_livro.sql`. Com data e hora, duas branches paralelas não escolhem o mesmo número, o que aconteceria com `V2`, `V3`...
- **Uma mudança por migration**, no mesmo PR que ajusta a entidade JPA correspondente, para o `validate` continuar passando.
- **Antes de abrir o PR**, rode os testes (*Como testar* no [README](README.md#como-testar)). Eles aplicam as migrations no seu `classbook_test`, e o CI repete isso em um banco vazio.
- **Se a aplicação não iniciar depois de um `git pull`** com `Detected resolved migration not applied to database`, chegou da `main` uma migration com data anterior à última que o seu banco já aplicou (acontece quando as duas branches criam migrations em paralelo). Com a aplicação parada, recrie o banco como superusuário (por exemplo, `psql -U postgres`); na próxima inicialização, o Flyway aplica todas as migrations, em ordem:

  ```sql
  DROP DATABASE classbook;
  CREATE DATABASE classbook OWNER classbook;
  ```

  Se o erro aparecer nos testes, faça o mesmo com o `classbook_test`.

## Segredos e arquivos locais

Nunca versione senhas, tokens ou o arquivo `.env`. Use o `.env.example` como modelo.
