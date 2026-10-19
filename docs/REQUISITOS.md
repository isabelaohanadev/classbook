# Requisitos do ClassBook

> **Revisado na Issue #2.** As perguntas em aberto do rascunho foram respondidas na seção [Decisões da revisão de requisitos](#decisões-da-revisão-de-requisitos). A conferência com o enunciado da disciplina é acompanhada na Issue #78. Alterações de escopo devem ser registradas aqui por PR.

## Objetivo do sistema

O ClassBook é uma biblioteca digital para uma escola de ensino médio. Ele organiza o acervo de **livros**, seus **autores** e **categorias**, e os **materiais digitais** (PDFs, vídeos, links) associados às **disciplinas**, permitindo cadastrar, consultar e pesquisar esse acervo por meio de uma API REST e de uma interface desktop simples.

## Escopo

### Incluído

- CRUD de Autor, Livro, Categoria, Disciplina e MaterialDigital.
- Relacionamentos: Livro–Autor, Livro–Categoria e MaterialDigital–Disciplina.
- Pesquisa avançada de livros (funcionalidade extra).
- Validação de entrada e tratamento padronizado de erros HTTP.
- Documentação das rotas com Swagger/OpenAPI.
- Interface gráfica desktop em JavaFX consumindo a API: catálogo, busca e filtros, formulários de cadastro e edição, detalhes de livros e links de materiais, mensagens de sucesso e erro.
- Migrations com Flyway, testes automatizados e CI com GitHub Actions.

### Fora do escopo

Autenticação e autorização, empréstimo ou reserva de livros, upload de arquivos, inteligência artificial, microsserviços, Docker obrigatório, deploy, CI/CD complexo, telas de login e dashboards.

## Requisitos funcionais

| ID | Requisito |
| --- | --- |
| RF01 | Cadastrar, editar, listar, consultar por ID e excluir **autores**. |
| RF02 | Cadastrar, editar, listar, consultar por ID e excluir **livros**, cada um vinculado a um autor e, opcionalmente, a uma categoria. |
| RF03 | Cadastrar, editar, listar, consultar por ID e excluir **categorias**. |
| RF04 | Cadastrar, editar, listar, consultar por ID e excluir **disciplinas**. |
| RF05 | Cadastrar, editar, listar, consultar por ID e excluir **materiais digitais**, cada um vinculado a uma disciplina. |
| RF06 | Pesquisar livros por título, autor e categoria, com filtros combináveis (ver *Funcionalidade extra*). |
| RF07 | Validar os dados de entrada e responder com mensagens claras em caso de erro. |
| RF08 | Responder erros com códigos HTTP coerentes: 400 (dados inválidos), 404 (ID inexistente), 409 (conflito) e 500 (erro inesperado), em formato padronizado. |
| RF09 | Impedir duplicidades relevantes: ISBN do livro e, conforme a decisão da Issue #46, código da disciplina e nome da categoria. |
| RF10 | Impedir a exclusão de um registro que ainda é referenciado (ex.: autor com livros), respondendo 409. |
| RF11 | Documentar todas as rotas no Swagger/OpenAPI. |
| RF12 | Oferecer interface desktop que permita navegar pelo catálogo, buscar e filtrar livros, cadastrar e editar registros, ver detalhes de um livro e abrir os links dos materiais digitais, exibindo mensagens de sucesso e de erro. |

## Requisitos não funcionais

| ID | Requisito |
| --- | --- |
| RNF01 | Java 21, Spring Boot 4.1.x e Maven. |
| RNF02 | PostgreSQL 16 ou superior; o schema é criado e evoluído somente por migrations Flyway (o Hibernate apenas valida). |
| RNF03 | API REST em JSON; entrada e saída por DTOs, sem expor entidades JPA. |
| RNF04 | Arquitetura em camadas: model, dto, repository, service, controller, exception e config. |
| RNF05 | Configuração por variáveis de ambiente; nenhum segredo no repositório. |
| RNF06 | Rotas e regras de negócio cobertas por testes automatizados; o CI roda `mvn -B verify` em todo PR para `main`. |
| RNF07 | O README deve permitir que um terceiro configure e execute o projeto sem ajuda. |
| RNF08 | Trabalho por GitHub Flow: branches curtas, Conventional Commits e PRs revisados pelo outro integrante. |
| RNF09 | Nomes de domínio e mensagens ao usuário em português. |
| RNF10 | Sem metas formais de desempenho; o volume esperado é pequeno (acervo de uma escola). |

## Funcionalidade extra: pesquisa avançada

Pesquisa de livros combinando filtros opcionais:

- **título**: contém o texto informado, sem diferenciar maiúsculas de minúsculas;
- **autor**: livros de um autor específico;
- **categoria**: livros de uma categoria específica.

Sem nenhum filtro, a pesquisa devolve todos os livros. Quando há mais de um filtro, todos precisam ser satisfeitos (E lógico). A rota proposta é `GET /api/livros` com parâmetros de consulta (ver [MODELAGEM](MODELAGEM.md#contrato-inicial-da-api-proposta)). Na interface JavaFX, a tela de catálogo oferece a busca e os filtros.

**Decidido na Issue #2:** a pesquisa avançada cobre **somente livros** (Issues #27 e #28). Os materiais digitais continuam acessíveis pela listagem e pela consulta por ID. Uma busca em materiais (por exemplo, por título e disciplina) fica fora desta versão e, se for desejada depois, precisa de uma Issue própria.

## Limitações assumidas

- Um livro tem **um único autor** nesta versão (coautoria exigiria uma tabela de associação; ver pontos em aberto na modelagem).
- Materiais digitais guardam apenas a **URL**; o sistema não armazena arquivos.
- Sem controle de usuários: qualquer cliente da API pode executar todas as operações.
- A interface é local (desktop) e depende de o backend estar em execução.
- A pesquisa avançada cobre somente livros; não há busca com filtros em materiais digitais.
- Sem paginação nas listagens; pode ser adicionada se o volume exigir.
- Datas de entrega e de avaliação não estão registradas aqui; serão incluídas somente quando forem informadas.

## Decisões da revisão de requisitos

Respostas às perguntas em aberto do rascunho (Issue #2):

| # | Pergunta | Decisão |
| --- | --- | --- |
| 1 | A busca avançada também deve cobrir materiais digitais? | **Não.** Cobre somente livros, como no backlog (#27 e #28). Ver *Funcionalidade extra* e *Limitações assumidas*. |
| 2 | A categoria é opcional em um livro? | **Sim.** O livro pode ser cadastrado sem categoria (RF02), como na [MODELAGEM](MODELAGEM.md#livro-livro). |
| 3 | O ISBN é obrigatório? | **Sim.** É obrigatório, único e guardado só com dígitos (10 ou 13). O tratamento de duplicidade é implementado na Issue #46. |
| 4 | As listagens precisam de paginação? | **Não.** O volume esperado é pequeno (RNF10). |
| 5 | Há alguma exigência específica da disciplina ou do professor que não esteja listada? | **Pendente.** O enunciado não está no repositório e a resposta não pode ser presumida. A conferência é feita na Issue #78. |
