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

Como configurar: **Settings → Branches → Add branch ruleset / protection rule** para `main`. Essas regras dependem do plano da conta e de permissão de administrador; o estado real de ativação é acompanhado na Issue de configuração das regras de proteção.

## Migrations (Flyway)

- Ficam em `src/main/resources/db/migration/` e **nunca** se edita uma migration já integrada na `main`: crie uma nova.
- A migration inicial (`V1__...`) cria as cinco tabelas, a partir da modelagem revisada em conjunto.
- As seguintes usam versão com data e hora, para que duas branches paralelas não escolham o mesmo número: `V202611051430__adiciona_indice_isbn.sql`.
- Se, depois de um `git pull`, o Flyway reclamar de uma migration "resolvida, mas não aplicada", recrie o seu banco de desenvolvimento.

## Segredos e arquivos locais

Nunca versione senhas, tokens ou o arquivo `.env`. Use o `.env.example` como modelo.
