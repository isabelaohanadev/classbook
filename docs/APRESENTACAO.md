# Roteiro da apresentação

Rascunho do roteiro pedido pela Issue #68, para a apresentação do dia **06/11/2026** (data confirmada em [PLANO_DE_TRABALHO](PLANO_DE_TRABALHO.md#cronograma)). Regras do enunciado, conferidas na Issue #78 (ver [REQUISITOS](REQUISITOS.md#conferência-com-o-enunciado-da-disciplina)):

- **Até 15 minutos no total:** até 10 para apresentar, demonstrar as principais funcionalidades e explicar a participação de cada um; até 5 para perguntas individuais.
- **Os dois participam** da apresentação e das perguntas.
- **Proibido** consultar IA ou responder pelo colega durante a apresentação e as perguntas; o próprio código pode ser consultado.

Este documento cobre os critérios possíveis sem a presença dos dois integrantes: tempo por parte, falas divididas e plano B. **Falta o item que só o ensaio real resolve:** cronometrar em voz alta e ajustar — ver [Pendente antes do dia 06/11](#pendente-antes-do-dia-0611).

## Tempo por parte (soma 10 minutos)

| Tempo | Parte | Quem fala |
| --- | --- | --- |
| 0:00–0:30 | Abertura: o que é o ClassBook, objetivo e stack (Java 21, Spring Boot, PostgreSQL, Flyway) | Isabela |
| 0:30–1:30 | Arquitetura e modelagem: camadas (`controller → service → repository → model`), as 5 entidades e os 3 relacionamentos | Juan |
| 1:30–4:00 | Demo: CRUD de Autor e de Livro, relacionamento Livro–Autor e pesquisa avançada (título, autor, categoria) | Isabela |
| 4:00–6:30 | Demo: CRUD de Categoria, Disciplina e MaterialDigital, relacionamento Material–Disciplina e tratamento de erros padronizado | Juan |
| 6:30–7:30 | Qualidade: migrations, CI (GitHub Actions) e GitHub Flow | Isabela ou Juan (combinar) |
| 7:30–9:00 | Participação de cada um (contribuições reais — ver [CONTRIBUICOES](CONTRIBUICOES.md)) | os dois, ~45s cada |
| 9:00–10:00 | Fechamento e abertura para perguntas | os dois |

## Falas, por bloco

### 1. Abertura (Isabela, 30s)

"O ClassBook é uma biblioteca escolar digital: API REST em Java e Spring Boot para cadastrar, editar, listar, consultar e excluir livros, autores, categorias, disciplinas e materiais digitais de uma escola de ensino médio, com pesquisa avançada de livros."

### 2. Arquitetura e modelagem (Juan, 1 min)

- Camadas: `controller → service → repository → model`, DTOs de entrada (`Request`) e saída (`Response`) — a API nunca expõe a entidade JPA direto (ver [ARQUITETURA](ARQUITETURA.md)).
- 5 entidades: Autor, Livro, Categoria, Disciplina, MaterialDigital. 3 relacionamentos: Livro–Autor, Livro–Categoria, MaterialDigital–Disciplina (ver [MODELAGEM](MODELAGEM.md)).
- Schema criado só por migrations Flyway; o Hibernate roda em modo `validate`, nunca altera o banco sozinho.

### 3. Demo — Autor, Livro e pesquisa avançada (Isabela, 2min30)

Pelo Swagger UI (`/swagger-ui.html`):

1. `POST /api/autores` — cadastra um autor.
2. `POST /api/livros` — cadastra um livro vinculado ao autor (e opcionalmente a uma categoria).
3. `GET /api/livros` — lista todos.
4. `GET /api/livros?titulo=...` — pesquisa por título (parcial, sem diferenciar maiúsculas).
5. `GET /api/livros?autorId=...&categoriaId=...` — combina filtros por autor e categoria.
6. `PUT /api/livros/{id}` e `DELETE /api/autores/{id}` — edição e exclusão; mostrar o erro 409 ao excluir um autor com livro vinculado.

### 4. Demo — Categoria, Disciplina, MaterialDigital e erros (Juan, 2min30)

1. `POST /api/categorias`, `POST /api/disciplinas` e `POST /api/materiais`: cadastro rápido dos três. O material leva o `disciplinaId` da disciplina recém-criada, e o código ` mat-1 ` volta como `MAT-1`.
2. `GET /api/materiais`: a lista traz cada material com a disciplina resumida (`id`, `nome`, `codigo`).
3. Provocar um erro de validação, com `POST /api/materiais` sem `titulo` e com `url` sem `http`, e mostrar o corpo padrão (`horario`, `status`, `erro`, `mensagem`, `caminho`, `campos`), com os dois campos listados.
4. `DELETE /api/disciplinas/{id}` da disciplina que tem o material: mostrar o `409` ("Disciplina X possui materiais vinculados."). Depois, excluir o material e repetir: agora responde `204`.

### 5. Qualidade (1 min)

- CI roda em todo PR e todo push na `main`: checkout, Java 21, `mvn -B verify`, PostgreSQL temporário — mostrar a aba *Actions* com o badge verde.
- GitHub Flow: branch curta por Issue, PR com `Closes #N` e CI verde antes de integrar. A proteção da `main` exige o check "Build e testes" e, desde 04/11, uma aprovação do outro integrante (#74). Antes disso, vários PRs entraram sem revisão formal, e a auditoria está registrada na #51. Se perguntarem, responder assim, sem dizer que a revisão sempre foi obrigatória.

### 6. Participação de cada um (1min30)

Basear nos números reais de [CONTRIBUICOES](CONTRIBUICOES.md#resumo-por-integrante) (Issues, PRs e funcionalidades de cada um), não inventar na hora.

### 7. Fechamento (1 min)

Recapitular o que foi mostrado e passar para as perguntas individuais.

## Plano B

- **Não demonstrar o cold start:** subir a aplicação e confirmar que o Swagger responde *antes* da apresentação começar.
- **Banco pronto com antecedência:** o PostgreSQL já rodando e testado minutos antes, com alguns registros de exemplo já cadastrados (não depender só de cadastrar tudo ao vivo).
- **Capturas de tela de reserva:** se a rede ou o notebook falharem, ter screenshots do Swagger com os cinco CRUDs e a pesquisa avançada já funcionando, como slides de apoio.
- **Se uma rota falhar ao vivo:** seguir para a próxima e explicar o que ela faria, sem gastar o tempo tentando depurar na hora.
- **Notebook de backup:** levar os dois notebooks configurados (banco, `.env`, IDE aberta), caso o principal tenha problema.

## Perguntas individuais (até 5 min)

Cada integrante precisa saber explicar o projeto inteiro, inclusive a parte do outro (Issue #69). Perguntas prováveis, para ensaiar:

- Por que o schema é criado só por migrations, e o que acontece se o Hibernate e o Flyway discordarem?
- Como o tratador global de exceções decide o código HTTP de cada erro?
- Por que a interface JavaFX é opcional e o que isso significa para a entrega (Issue #93)?
- O que a pesquisa avançada faz quando nenhum filtro é informado? E quando o título tem `%` ou `_`?
- Como o CI garante que a `main` não quebra, e o que a proteção de branch impede?
- O que cada um fez de fato no projeto (sem consultar o CONTRIBUICOES.md na hora)?

**Proibido na apresentação e nas perguntas:** consultar ferramentas de IA ou responder pela parte do colega.

### Guia de estudo do Juan (Issue #69)

O que o Juan precisa saber explicar sozinho, inclusive da frente da Isabela, com o arquivo para conferir no código durante o estudo. No dia, só o próprio código pode ser consultado.

- **Uma rota de ponta a ponta (`POST /api/livros`):**
  1. `LivroController.criar` recebe o `LivroRequest` com `@Valid`. O construtor do record tira hífens e espaços do ISBN antes da validação, e o `@Pattern` exige 10 ou 13 dígitos.
  2. `LivroService.criar` confere o ISBN repetido (`409`). Depois, em `preencher`, confere o ano no futuro, o autor inexistente e a categoria inexistente (`400`).
  3. O `LivroRepository` grava, e o `LivroResponse.de` monta a resposta com o autor e a categoria resumidos.
  4. O controller responde `201` com o header `Location`. Qualquer exceção vai para o `TratadorGlobalDeExcecoes`, que monta o `ErroResponse`.
- **Relacionamentos do Livro (`model/Livro.java`):** o autor é obrigatório (`optional = false`), e a categoria é opcional. Os dois usam `LAZY`, e o `@EntityGraph` do `LivroRepository` carrega autor e categoria na mesma consulta da listagem, para não fazer uma consulta por livro (o problema N+1). No `PUT`, um `categoriaId` nulo tira o livro da categoria.
- **Pesquisa avançada (`LivroRepository.pesquisar`):**
  - Os três filtros (`titulo`, `autorId` e `categoriaId`) são opcionais e se combinam com E lógico, e o resultado vem ordenado por título.
  - Um `autorId` inexistente devolve lista vazia, e não erro.
  - O título vazio vira `""`, porque o PostgreSQL não consegue tipar um parâmetro nulo dentro do `lower(concat(...))`.
  - `%` e `_` são escapados no `LivroService`, para serem procurados como texto (#148).
- **Exclusões bloqueadas (`409`):** cada uma é conferida no service antes de excluir, com a chave estrangeira `RESTRICT` da V1 como rede de segurança.
  - Autor com livros: `AutorService.excluir`, com `existsByAutorId`.
  - Categoria com livros: `CategoriaService.excluir`.
  - Disciplina com materiais: `DisciplinaService.excluir`.
- **Migrations:** só existe a `V1__criar_tabelas_iniciais.sql`. O Hibernate roda com `ddl-auto=validate`: se uma entidade não bater com a tabela, a aplicação não sobe. Uma migration integrada nunca é editada, porque o Flyway confere o checksum. O `MigrationsTests` garante que todas as migrations foram aplicadas (#49).
- **CI e proteção da `main`:**
  - O `.github/workflows/ci.yml` roda `mvn -B verify` com um PostgreSQL 16 temporário em todo PR para a `main` e em todo push nela.
  - A proteção exige o check "Build e testes" e uma aprovação (#74).
  - A `main` já ficou vermelha uma vez: o #136 e o #140 passaram no CI separados, mas juntos quebraram a compilação dos testes. O #145 corrigiu.
- **GitHub Flow:** são os sete passos do `CONTRIBUTING.md`, seção *Fluxo de trabalho*. Os nomes de branch são `feature/`, `fix/`, `docs/`, `test/` e `chore/`, e os commits seguem `tipo: descrição` (Conventional Commits).

## Pendente antes do dia 06/11

- [ ] **Ensaiar em voz alta, cronometrando cada bloco** — isto não pode ser feito por este documento; exige os dois integrantes. Ajustar os tempos acima pelo que o ensaio mostrar.
- [ ] Confirmar quem apresenta primeiro, o notebook usado e o acesso à rede/projetor no local.
- [ ] Cadastrar os dados de exemplo no banco da demo antes do ensaio final.
- [ ] Ensaio de perguntas (#69): cada um pergunta ao outro sobre a própria frente, usando a lista acima e o guia de estudo, sem consultar nada além do código.
