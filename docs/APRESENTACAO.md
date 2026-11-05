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

1. `POST /api/categorias`, `POST /api/disciplinas` (com o código ` mat-1 `, que volta como `MAT-1`) e `POST /api/materiais` (com o `disciplinaId` da disciplina recém-criada) — cadastro rápido dos três.
2. `GET /api/materiais` — lista com o link do material e a disciplina resumida (`id`, `nome`, `codigo`).
3. Provocar um erro de validação (`POST /api/materiais` sem `disciplinaId` e com `url` sem `https://`) e mostrar o corpo padrão (`horario`, `status`, `erro`, `mensagem`, `caminho`, `campos`), com os dois campos listados em `campos`.
4. `DELETE /api/disciplinas/{id}` da disciplina que tem o material — mostrar o `409` ("Disciplina X possui materiais vinculados."). Depois, `DELETE /api/materiais/{id}` e de novo o `DELETE` da disciplina, agora `204`.

### 5. Qualidade (1 min)

- CI roda em todo PR e todo push na `main`: checkout, Java 21, `mvn -B verify`, PostgreSQL temporário — mostrar a aba *Actions* com o badge verde.
- GitHub Flow: branch curta por Issue, PR com `Closes #N` e CI verde antes de integrar. A proteção da `main` recusa push direto e PR com CI vermelho, e exige 1 aprovação desde 04/11 (Issue #74). Antes disso, a revisão cruzada era combinada, e a auditoria da Issue #51 registra os PRs que entraram sem ela. Não dizer que a revisão sempre foi obrigatória.

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

### Respostas de referência, para estudar antes (Issue #69)

Cada resposta aponta para o código ou o documento onde está o detalhe, para cada um conferir a resposta lendo o próprio código, e não decorando este texto. No dia, só o código pode ser consultado.

| Pergunta | Resposta curta | Onde conferir |
| --- | --- | --- |
| Camadas | O controller recebe o `Request` com `@Valid` e chama o service. O service aplica as regras que dependem do banco e lança as exceções de domínio. O repository é Spring Data JPA, e a entidade nunca sai da API: a conversão para `Response` acontece dentro da transação. | `docs/ARQUITETURA.md`; `controller/`, `service/`, `repository/`, `dto/` |
| Uma rota de ponta a ponta: `POST /api/materiais` | O `MaterialDigitalController.criar` valida o `MaterialDigitalRequest` (título, formato, URL `http(s)` e `disciplinaId` obrigatórios). O `MaterialDigitalService.criar` busca a disciplina: se ela não existe, lança `DadosInvalidosException` (`400`). Depois grava, converte para `MaterialDigitalResponse` com a disciplina resumida e o controller responde `201` com `Location`. | `MaterialDigitalController`, `MaterialDigitalService`, `MaterialDigitalResponse` |
| Por que só migrations? E se Hibernate e Flyway discordarem? | O Flyway cria e altera o schema (`V1__criar_tabelas_iniciais.sql`). O Hibernate roda com `ddl-auto=validate`: se uma entidade não bate com a tabela, a aplicação não sobe (`Schema-validation`). O `MigrationsTests` confere que todas as migrations foram aplicadas. Uma migration integrada nunca é editada: muda o checksum. | `db/migration/`, `application.properties`, `MigrationsTests`, `CONTRIBUTING.md#migrations-flyway` |
| Como o tratador decide o código HTTP? | O `TratadorGlobalDeExcecoes` (`@RestControllerAdvice`) mapeia cada exceção: `RecursoNaoEncontradoException` → `404`, `DadosInvalidosException` e `@Valid` → `400` (com `campos`), `ConflitoException` e violação de integridade → `409`, rota inexistente → `404`, qualquer outra → `500` com mensagem genérica. Todas saem no `ErroResponse`. | `exception/TratadorGlobalDeExcecoes`, README *Erros da API* |
| Duplicidades e vínculos | O service confere antes de gravar: ISBN, nome de categoria (sem diferenciar maiúsculas) e código de disciplina repetidos → `409`; excluir autor ou categoria com livros, ou disciplina com materiais → `409`. Os `UNIQUE` e as FKs com `ON DELETE RESTRICT` do banco são a rede de segurança. | `LivroService`, `CategoriaService`, `DisciplinaService`, `AutorService`, `V1__criar_tabelas_iniciais.sql` |
| Pesquisa avançada (frente da Isabela) | `GET /api/livros` com `titulo`, `autorId` e `categoriaId` opcionais e combinados com E. Sem filtro, devolve todos, ordenados por título. O título é procurado em qualquer posição, sem diferenciar maiúsculas, e `%` e `_` contam como texto (o service escapa os curingas do `LIKE`). Um ID inexistente devolve lista vazia, não erro. | `LivroRepository.pesquisar`, `LivroService.listar` |
| CI e proteção da `main` | Todo PR para a `main` e todo push nela rodam `mvn -B verify` com um PostgreSQL 16 temporário. A `main` recusa push direto e merge com o check "Build e testes" vermelho, e exige 1 aprovação desde 04/11. O PR #135 mostrou um teste falhando de propósito e o check voltando ao verde depois da correção (Issue #75). | `.github/workflows/ci.yml`, Issues #74 e #75 |
| GitHub Flow | Uma branch curta por Issue a partir da `main`, commits no padrão Conventional Commits, PR com `Closes #N`, CI verde, revisão do outro integrante, merge e exclusão da branch. | `CONTRIBUTING.md` |
| Interface JavaFX | É opcional (decisão da Issue #93), porque o enunciado não exige interface. É um projeto Maven separado em `desktop/`, que só fala com a API REST: um cliente HTTP (`ClienteApi`) faz as chamadas fora da thread da tela. | `desktop/README.md`, Issue #93 |

O ensaio das perguntas (critério da #69) continua pendente, junto com o ensaio cronometrado: cada um responde às perguntas acima sem olhar, inclusive as da frente do outro.

## Pendente antes do dia 06/11

- [ ] **Ensaiar em voz alta, cronometrando cada bloco** — isto não pode ser feito por este documento; exige os dois integrantes. Ajustar os tempos acima pelo que o ensaio mostrar.
- [ ] Confirmar quem apresenta primeiro, o notebook usado e o acesso à rede/projetor no local.
- [ ] Cadastrar os dados de exemplo no banco da demo antes do ensaio final.
