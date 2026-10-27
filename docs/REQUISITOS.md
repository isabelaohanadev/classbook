# Requisitos do ClassBook

> **Revisado na Issue #2 e conferido com o enunciado da disciplina na Issue #78.** As perguntas em aberto do rascunho foram respondidas na seção [Decisões da revisão de requisitos](#decisões-da-revisão-de-requisitos), e o resultado da conferência está em [Conferência com o enunciado da disciplina](#conferência-com-o-enunciado-da-disciplina). Alterações de escopo devem ser registradas aqui por PR.

## Objetivo do sistema

O ClassBook é uma biblioteca digital para uma escola de ensino médio. Ele organiza o acervo de **livros**, seus **autores** e **categorias**, e os **materiais digitais** (PDFs, vídeos, links) associados às **disciplinas**, permitindo cadastrar, consultar e pesquisar esse acervo por meio de uma API REST e de uma interface desktop simples.

## Escopo

### Incluído

- CRUD de Autor, Livro, Categoria, Disciplina e MaterialDigital.
- Relacionamentos: Livro–Autor, Livro–Categoria e MaterialDigital–Disciplina.
- Pesquisa avançada de livros (funcionalidade extra).
- Validação de entrada e tratamento padronizado de erros HTTP.
- Documentação das rotas com Swagger/OpenAPI.
- Interface gráfica desktop em JavaFX consumindo a API: catálogo, busca e filtros, formulários de cadastro e edição, detalhes de livros e links de materiais, mensagens de sucesso e erro. O enunciado não exige interface visual; a permanência dela no escopo é decidida na Issue #93.
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
| RF08 | Responder erros com códigos HTTP coerentes: 400 (dados inválidos, inclusive o ID de autor, categoria ou disciplina inexistente no corpo), 404 (ID inexistente na rota), 409 (conflito) e 500 (erro inesperado), em formato padronizado. |
| RF09 | Impedir duplicidades relevantes: ISBN do livro, código da disciplina e nome da categoria (decisão da Issue #3, ver [MODELAGEM](MODELAGEM.md#enums-e-unicidade)). |
| RF10 | Impedir a exclusão de um registro que ainda é referenciado (ex.: autor com livros), respondendo 409. |
| RF11 | Documentar todas as rotas no Swagger/OpenAPI. |
| RF12 | Oferecer interface desktop que permita navegar pelo catálogo, buscar e filtrar livros, cadastrar e editar registros, ver detalhes de um livro e abrir os links dos materiais digitais, exibindo mensagens de sucesso e de erro. Não é exigido pelo enunciado (ver Issue #93). |

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

- Um livro tem **um único autor** nesta versão (coautoria exigiria uma tabela de associação; ver [decisões da modelagem](MODELAGEM.md#decisões-da-revisão-conjunta)).
- Materiais digitais guardam apenas a **URL**; o sistema não armazena arquivos.
- Sem controle de usuários: qualquer cliente da API pode executar todas as operações.
- A interface é local (desktop) e depende de o backend estar em execução.
- A pesquisa avançada cobre somente livros; não há busca com filtros em materiais digitais.
- Sem paginação nas listagens; pode ser adicionada se o volume exigir.
- Datas de entrega e de avaliação não estão registradas aqui; serão incluídas somente quando forem informadas. O enunciado também não as traz (ver Issue #94).

## Decisões da revisão de requisitos

Respostas às perguntas em aberto do rascunho (Issue #2):

| # | Pergunta | Decisão |
| --- | --- | --- |
| 1 | A busca avançada também deve cobrir materiais digitais? | **Não.** Cobre somente livros, como no backlog (#27 e #28). Ver *Funcionalidade extra* e *Limitações assumidas*. |
| 2 | A categoria é opcional em um livro? | **Sim.** O livro pode ser cadastrado sem categoria (RF02), como na [MODELAGEM](MODELAGEM.md#livro-livro). |
| 3 | O ISBN é obrigatório? | **Sim.** É obrigatório, único e guardado sem hífens (10 ou 13 caracteres; o ISBN-10 pode terminar em `X`). O tratamento de duplicidade é implementado na Issue #46. |
| 4 | As listagens precisam de paginação? | **Não.** O volume esperado é pequeno (RNF10). |
| 5 | Há alguma exigência específica da disciplina ou do professor que não esteja listada? | **Sim, três pontos.** Conferido na Issue #78, item a item, contra o enunciado; o resultado está em [Conferência com o enunciado da disciplina](#conferência-com-o-enunciado-da-disciplina). Viraram Issues: #92 (apresentar a funcionalidade extra ao professor), #93 (a interface visual não é exigida) e #94 (datas do cronograma). |

### Conferência com o enunciado da disciplina

Feita na Issue #78, em 27/10/2026. O enunciado publicado pelo professor (não versionado no repositório) foi comparado com este documento, a [MODELAGEM](MODELAGEM.md), a [ARQUITETURA](ARQUITETURA.md), o [PLANO_DE_TRABALHO](PLANO_DE_TRABALHO.md), as [CONTRIBUICOES](CONTRIBUICOES.md) e as Issues. A coluna *Situação* diz se a exigência está registrada nos documentos ou no backlog, não se já foi implementada. Nenhuma exigência foi acrescentada sem constar no enunciado.

**Requisitos do projeto**

| Exigência do enunciado | Onde está | Situação |
| --- | --- | --- |
| API REST para organizar, disponibilizar e consultar livros e outros materiais digitais, facilitando o acesso dos estudantes; cada equipe define os recursos | *Objetivo do sistema* e *Escopo* | Registrada |
| Definir os requisitos, as entidades e o cronograma de desenvolvimento, indicando as responsabilidades de cada integrante | Este documento, [MODELAGEM](MODELAGEM.md) e [PLANO_DE_TRABALHO](PLANO_DE_TRABALHO.md) | **Em parte:** o plano tem etapas, responsáveis e ordem, mas não tem datas. Issue #94 |
| Cinco CRUDs completos (cadastrar, editar, listar, consultar por ID e excluir); o CRUD de usuário das aulas não conta | RF01 a RF05 | Registrada; nenhum dos cinco é de usuário |
| Pelo menos uma funcionalidade além dos CRUDs, relacionada à proposta | RF06 e *Funcionalidade extra* | Registrada |
| A ideia da funcionalidade extra deve ser apresentada ao professor, para alinhar escopo e complexidade | — | **Não estava registrada.** Issue #92 |
| Entidades com atributos e relacionamentos coerentes, com pelo menos 4 atributos além do ID (menos, só com justificativa) | [MODELAGEM](MODELAGEM.md#entidades-e-atributos): 4 ou 5 atributos por entidade e três relacionamentos | Registrada |
| Camadas Model, DTO, Repository, Service e Controller | RNF04 e [ARQUITETURA](ARQUITETURA.md) | Registrada |
| Persistir os dados em banco, com criação e alteração das tabelas por migrations | RNF02 | Registrada |
| Validar os dados recebidos e tratar os erros com mensagens claras e códigos HTTP adequados | RF07 e RF08 | Registrada |
| Documentar todas as rotas no Swagger | RF11 | Registrada |
| Projeto no GitHub, com instruções de execução e contribuições identificáveis de todos os integrantes | RNF07, RNF08 e [CONTRIBUICOES](CONTRIBUICOES.md) | Registrada |
| Linguagem livre | RNF01 | Registrada (Java 21) |
| A interface visual não é necessária | *Escopo* e RF12 incluem a interface JavaFX | **Vai além do exigido**, e as Issues #63, #64, #65, #67 e #68 dependem da #62 (teste da interface). Issue #93 |

**Acompanhamento, uso de IA, apresentação e entrega**

Estas exigências não constavam em nenhum documento e ficam registradas aqui:

| Exigência do enunciado | Onde é tratada | Situação |
| --- | --- | --- |
| Acompanhamento semanal, conforme o cronograma divulgado pelo professor: o que cada integrante desenvolveu, as dificuldades e o plano da semana seguinte. Não tem nota própria | Datas na Issue #94 | Registrada aqui |
| Contribuições registradas no GitHub ao longo do desenvolvimento | RNF08 | Registrada |
| Em atividade feita em conjunto, indicar quem participou e o que cada pessoa fez | [CONTRIBUICOES](CONTRIBUICOES.md) e Issue #64 | Registrada aqui; o documento de contribuições ainda não pede isso |
| Todos contribuem com a implementação; participar só da documentação não dá a pontuação integral em contribuições | [PLANO_DE_TRABALHO](PLANO_DE_TRABALHO.md#responsabilidades): cada integrante tem CRUDs | Registrada |
| Uso de IA permitido; informar a ferramenta, para que foi usada e como o resultado foi verificado | [CONTRIBUICOES](CONTRIBUICOES.md#registro-de-uso-de-ferramentas-de-ia) e Issue #65 | Registrada |
| Cada integrante compreende e sabe explicar o código que entregar. Na apresentação e nas perguntas, não se consulta IA nem se responde pelo colega; o próprio código pode ser consultado | Issue #69 | Registrada aqui; a #69 não citava a proibição de IA nem a de responder pelo colega |
| Apresentação de até 15 minutos: até 10 para apresentar, demonstrar as principais funcionalidades e explicar a participação de cada um, e até 5 para perguntas individuais. Todos participam | Issues #68 e #69 | Registrada |
| O sistema está pronto para execução no início do horário reservado | Issues #67 e #68 | Registrada |
| Entrega no Moodle de um ZIP com: código-fonte; nomes dos integrantes e usuários no GitHub; link do repositório e o commit da versão entregue; instruções de configuração e execução; breve descrição das contribuições de cada integrante; registro do uso de IA, quando houver | Issue #70 | Registrada; esta é a lista que a #70 confere |
| Repositório acessível ao professor | Repositório público | Atendida (conferido em 27/10/2026) |

**Avaliação (10 pontos)**

Registrada para orientar as prioridades. A nota de cada estudante é a soma das três partes, então integrantes da mesma equipe podem ter notas diferentes.

| Parte | Pontos | Critérios |
| --- | --- | --- |
| Sistema (igual para a equipe) | 4,0 | Funcionamento dos cinco CRUDs (1,0); funcionalidade além dos CRUDs (1,0); modelagem e organização das camadas (0,5); persistência e migrations (0,5); validações e tratamento de erros (0,5); Swagger e instruções de execução (0,5) |
| Apresentação e domínio do projeto (individual) | 3,0 | Clareza ao apresentar a própria participação (1,0); compreensão do código e explicação das decisões (1,0); respostas às perguntas sobre a aplicação (1,0) |
| Contribuições no GitHub (individual) | 3,0 | Contribuições relevantes para a implementação (1,0); participação ao longo do desenvolvimento, de acordo com as responsabilidades assumidas (1,0); registros identificáveis e mensagens de commit que expliquem as alterações (1,0) |

O número de commits, por si só, não define a nota. Nenhum critério menciona interface visual.

**Observação:** o item *inteligência artificial* em *Fora do escopo* trata de funcionalidades do sistema. O uso de ferramentas de IA durante o desenvolvimento é permitido pelo enunciado e é registrado em [CONTRIBUICOES](CONTRIBUICOES.md#registro-de-uso-de-ferramentas-de-ia).
