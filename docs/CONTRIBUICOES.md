# Contribuições individuais

Registro das contribuições de cada integrante. **Preencha somente com o que realmente aconteceu**, a cada PR integrado. Os números das Issues e dos PRs permitem conferir tudo no GitHub (commits, revisões e Actions).

## Resumo por integrante

| Integrante | Issues realizadas | Pull requests | Funcionalidades implementadas | Testes executados |
| --- | --- | --- | --- | --- |
| Isabela Ohana (@isabelaohanadev) | 33: #3, #4, #5, #8, #9, #11–#29, #45, #46, #48, #54, #55, #71, #73, #93, #94 | 35 (#76–#145, ver detalhamento) | Estrutura inicial do projeto, CI (#71) e `CONTRIBUTING.md` (#73); migration inicial e arquitetura em camadas; CRUD completo de Autor e de Livro; relacionamento Livro–Autor e Livro–Categoria; pesquisa de livros por título e filtros por autor/categoria; duplicidade de ISBN e exclusão de categoria em uso; Swagger de Autor e Livro; decisão de tornar a interface JavaFX opcional (#93); estrutura do módulo JavaFX e tela principal/navegação; registro do cronograma e do uso de IA | Suíte completa (`./mvnw verify`) verde antes de cada um dos 35 PRs, local e no CI, contra PostgreSQL 16 (os #102–#117 e o #138 não têm CI no próprio commit e passaram pelo CI no push da `main` após o merge; ver a auditoria na Issue #51) |
| Juan Ribeiro (@JuanRiber) | 30: #2, #3, #4, #6, #7, #30–#44, #47, #48 (parte: #134), #49, #52, #59, #72, #75 (parte: #135), #78, #92, #94 | 29 (#79–#149, ver detalhamento) | Revisão de requisitos e da modelagem de Categoria, Disciplina e MaterialDigital; configuração de Java, dependências e PostgreSQL; CRUD completo de Categoria, Disciplina e MaterialDigital; relacionamento Material–Disciplina; validações de entrada, tratador global de exceções e padronização de erros; Swagger de Categoria, Disciplina e MaterialDigital; teste das migrations em banco vazio; cliente HTTP da interface desktop; correções de pesquisa e de regressão pós-merge | Suíte completa (`./mvnw verify`) verde localmente, contra PostgreSQL 16, antes de cada um dos 29 PRs; no CI, em todos os PRs para a `main` (os #123–#128, mesclados nas branches da pilha, passaram pelo CI no #122); testes de relacionamento e validação também conferidos com mutação manual |

As Issues #3, #4, #48, #75 e #94 têm trabalho dos dois integrantes porque são tarefas conjuntas (revisão cruzada da modelagem, Swagger, teste do CI e registro do cronograma, cada um fazendo a parte que lhe cabia).

## Detalhamento por pull request

| PR | Autor | Issue(s) | O que mudou | Revisor | Testes executados |
| --- | --- | --- | --- | --- | --- |
| [#76](../../pull/76) | Isabela | #71, #73 | Estrutura inicial do projeto, documentação e CI | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#77](../../pull/77) | Isabela | — | Adiciona licença MIT | Juan | CI verde (`mvn -B verify`) |
| [#79](../../pull/79) | Juan | #2 | Revisa requisitos e responde perguntas em aberto | Isabela | CI verde (`mvn -B verify`) |
| [#80](../../pull/80) | Isabela | #3 | Revisa a modelagem de Autor e Livro e registra as decisões | Juan | CI verde (`mvn -B verify`) |
| [#81](../../pull/81) | Juan | #3 | Revisa a modelagem de Categoria, Disciplina e MaterialDigital | Isabela | CI verde (`mvn -B verify`) |
| [#82](../../pull/82) | Isabela | #4 | Define os relacionamentos de Livro e o contrato de Autor e Livro | Juan | CI verde (`mvn -B verify`) |
| [#83](../../pull/83) | Juan | #4 | Define o relacionamento de MaterialDigital e o contrato de Categoria, Disciplina e MaterialDigital | Isabela | CI verde (`mvn -B verify`) |
| [#84](../../pull/84) | Isabela | #5 | Conclui o projeto Spring Boot com Maven | Juan | CI verde (`mvn -B verify`) |
| [#85](../../pull/85) | Juan | #6 | Documenta as regras de Java e dependências | — | CI verde (`mvn -B verify`) |
| [#86](../../pull/86) | Juan | #7 | Conclui a configuração do PostgreSQL e das variáveis de ambiente | Isabela | CI verde (`mvn -B verify`) |
| [#90](../../pull/90) | Isabela | #8 | Cria a migration inicial do Flyway com as cinco tabelas | Juan | CI verde (`mvn -B verify`) |
| [#91](../../pull/91) | Isabela | #9 | Registra a arquitetura em camadas e as convenções do backend | Juan | CI verde (`mvn -B verify`) |
| [#95](../../pull/95) | Juan | #78 | Confere os requisitos com o enunciado da disciplina | — | CI verde (`mvn -B verify`) |
| [#96](../../pull/96) | Juan | #30 | Cria a entidade Categoria e seus DTOs | — | CI verde (`mvn -B verify`) |
| [#97](../../pull/97) | Juan | #92 | Registra a aprovação da pesquisa avançada pelo professor | — | CI verde (`mvn -B verify`) |
| [#98](../../pull/98) | Isabela | #93 | Torna a interface JavaFX opcional e pós-avaliação | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#99](../../pull/99) | Juan | #94 | Prepara o registro do cronograma de desenvolvimento (Issue #94) | — | CI verde (`mvn -B verify`) |
| [#100](../../pull/100) | Juan | #94 | Atualiza a Etapa 5 na tabela de períodos do cronograma | — | CI verde (`mvn -B verify`) |
| [#101](../../pull/101) | Isabela | #11 | Cria a entidade Autor e seus DTOs | Juan | CI verde (`mvn -B verify`) |
| [#102](../../pull/102) | Isabela | #12 | Cria o AutorRepository | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#103](../../pull/103) | Isabela | #13 | Implementa o AutorService | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#104](../../pull/104) | Isabela | #14 | Cria o AutorController | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#105](../../pull/105) | Isabela | #15 | Implementa o cadastro de autor | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#106](../../pull/106) | Isabela | #16 | Implementa a listagem de autores | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#107](../../pull/107) | Isabela | #17 | Implementa a consulta de autor por id | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#108](../../pull/108) | Isabela | #18 | Implementa a edição de autor | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#109](../../pull/109) | Isabela | #19 | Implementa a exclusão de autor | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#110](../../pull/110) | Isabela | #20 | Cria a entidade Livro e seus DTOs | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#111](../../pull/111) | Isabela | #21 | Cria o LivroRepository | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#112](../../pull/112) | Isabela | #22 | Implementa o LivroService | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#113](../../pull/113) | Isabela | #23 | Cria o LivroController | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#114](../../pull/114) | Isabela | #24 | Implementa CRUD de livros | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#115](../../pull/115) | Isabela | #25 | Configura o relacionamento entre Livro e Autor | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#116](../../pull/116) | Isabela | #27 | Implementa a pesquisa de livros por título | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#117](../../pull/117) | Isabela | #65 | Registra o uso do Claude Code na Etapa 2 | Juan | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#118](../../pull/118) | Isabela | #94 | Registra a data da apresentação na Issue #94 | Juan | CI verde (`mvn -B verify`) |
| [#119](../../pull/119) | Juan | #31 | Cria CategoriaRepository, CategoriaService e CategoriaController | — | CI verde (`mvn -B verify`) |
| [#121](../../pull/121) | Juan | #33 | Cria a entidade Disciplina e seus DTOs | — | CI verde (`mvn -B verify`) |
| [#122](../../pull/122) | Juan | #34, #35, #36, #37, #38, #39, #41 | Integra Disciplina, MaterialDigital e os testes da Etapa 3 | — | CI verde (`mvn -B verify`) |
| [#123](../../pull/123) | Juan | #35 | Implementa as cinco operações do CRUD de disciplinas | — | Sem CI no próprio commit (mesclado na branch da pilha); CI no #122 |
| [#124](../../pull/124) | Juan | #36 | Cria a entidade MaterialDigital e seus DTOs | — | Sem CI no próprio commit (mesclado na branch da pilha); CI no #122 |
| [#125](../../pull/125) | Juan | #37 | Cria MaterialDigitalRepository, MaterialDigitalService e MaterialDigitalController | — | Sem CI no próprio commit (mesclado na branch da pilha); CI no #122 |
| [#126](../../pull/126) | Juan | #38 | Implementa as cinco operações do CRUD de materiais digitais | — | Sem CI no próprio commit (mesclado na branch da pilha); CI no #122 |
| [#127](../../pull/127) | Juan | #39 | Bloqueia a exclusão de disciplina com materiais e testa o relacionamento | — | Sem CI no próprio commit (mesclado na branch da pilha); CI no #122 |
| [#128](../../pull/128) | Juan | #41 | Cobre as rotas e as validações da Etapa 3 sem depender de dados do banco | — | Sem CI no próprio commit (mesclado na branch da pilha); CI no #122 |
| [#129](../../pull/129) | Juan | #32 | Implementa as cinco operações do CRUD de categorias | — | CI verde (`mvn -B verify`) |
| [#130](../../pull/130) | Juan | #43, #44 | Cria o tratador global de exceções com formato de erro padronizado | — | CI verde (`mvn -B verify`) |
| [#131](../../pull/131) | Juan | #42 | Confere o ano de publicação no futuro e detalha a mensagem do ISBN | — | CI verde (`mvn -B verify`) |
| [#132](../../pull/132) | Juan | #47 | Configura a documentação Swagger/OpenAPI | — | CI verde (`mvn -B verify`) |
| [#133](../../pull/133) | Juan | #65 | Registra o uso do Claude Code pelo Juan | — | CI verde (`mvn -B verify`) |
| [#134](../../pull/134) | Juan | #48 | Documenta no Swagger as rotas de categorias, disciplinas e materiais | — | CI verde (`mvn -B verify`) |
| [#136](../../pull/136) | Isabela | #46 | Trata duplicidade de ISBN no cadastro e na edição de livros | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#137](../../pull/137) | Isabela | #54 | Define a estrutura do módulo JavaFX em desktop/ | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#138](../../pull/138) | Isabela | #55 | Cria a tela principal e a navegação do desktop | Juan (depois do merge) | Sem CI no próprio commit; CI no push da `main` após o merge |
| [#140](../../pull/140) | Isabela | #26 | Configura o relacionamento entre livro e categoria | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#141](../../pull/141) | Isabela | #45 | Bloqueia a exclusão de categoria com livros vinculados | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#142](../../pull/142) | Isabela | #28 | Implementa filtros por autor e categoria na pesquisa de livros | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#143](../../pull/143) | Isabela | #29 | Consolida as combinações de filtro de título, autor e categoria | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#144](../../pull/144) | Isabela | #48 | Documenta no Swagger as rotas de autores e livros | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#145](../../pull/145) | Isabela | #26, #46 | Corrige chamadas de LivroRequest sem categoriaId após o merge do #140 | Juan (depois do merge) | CI verde (`mvn -B verify`) |
| [#146](../../pull/146) | Juan | #49 | Confere que todas as migrations sobem num banco vazio | — | CI verde (`mvn -B verify`) |
| [#147](../../pull/147) | Juan | #59 | Implementa o cliente HTTP da interface desktop | — | CI verde (`mvn -B verify`) |
| [#148](../../pull/148) | Juan | #28, #51 | Procura %, _ e a barra invertida como texto na pesquisa de livros | — | CI verde (`mvn -B verify`) |
| [#149](../../pull/149) | Juan | #94 | Registra o período realizado das Etapas 3 e 4 no cronograma | — | CI verde (`mvn -B verify`) |

As Issues #1, #5, #10, #40, #52, #72 e #75 não aparecem porque foram concluídas sem um PR que as fechasse automaticamente (verificação direta, registrada em comentário da própria Issue) — conferido pela API do GitHub (`closedByPullRequestsReferences`), não presumido.

## Registro de uso de ferramentas de IA

Toda ferramenta de IA usada no projeto deve ser registrada aqui, com o que foi feito e como o resultado foi revisado (Issue #65).

| Data | Ferramenta | Quem usou | Para quê | Revisão humana |
| --- | --- | --- | --- | --- |
| 31/10/2026 | Claude Code | Isabela Ohana | Implementar o CRUD de Autor e de Livro da Etapa 2 (Issues #11–#25 e #27: entidades, DTOs, repositories, services, controllers, relacionamento Livro–Autor e pesquisa por título), seguindo as convenções de `docs/ARQUITETURA.md` e `docs/MODELAGEM.md` | Isabela acompanhou e direcionou a execução nesta sessão; a suíte de testes completa (`./mvnw verify`) rodou localmente contra um PostgreSQL 16, com resultado verde, antes de cada um dos 16 PRs (#101–#116). A revisão cruzada do Juan foi feita em 31/10/2026: os PRs foram aprovados, com comentários (ver a linha seguinte) |
| 31/10 a 01/11/2026 | Claude Code | Juan Ribeiro | Revisar os PRs #101–#118 da Isabela; implementar a Etapa 3 (Issues #31–#41: Categoria, Disciplina e MaterialDigital, relacionamento Material–Disciplina, validação manual das rotas e testes das rotas e validações) e as Issues #42, #43, #44 e #47 (validações de entrada, tratador global de exceções, formato de erro e Swagger), seguindo `docs/ARQUITETURA.md` e `docs/MODELAGEM.md` | Juan definiu as tarefas e acompanhou a execução nesta sessão. A suíte completa (`./mvnw verify`) passou antes de cada PR: no CI no #119 e localmente, contra um PostgreSQL 16, do #121 ao #132. Os testes de relacionamento e de validação foram conferidos com mutações (um defeito introduzido de propósito para ver o teste falhar), e as rotas da Etapa 3 foram validadas manualmente na #40. A revisão cruzada da Isabela está pendente nesses PRs |
| 02 a 04/11/2026 | Claude Code | Juan Ribeiro | Concluir a integração da Etapa 3 (#122) e as Issues #49 (migrations em banco vazio), #52 (roteiro de ponta a ponta dos cinco CRUDs), #59 (cliente HTTP do desktop) e a parte do Juan na #48, #50, #51, #68, #69, #75 e #94; revisar e aprovar os PRs da Isabela (#76, #98 e #136 a #156), corrigir o defeito encontrado na revisão do #142 (#148) e auditar os PRs integrados (#51) | Juan definiu as tarefas e acompanhou a execução. Antes de cada PR: suíte completa (`./mvnw verify`) verde localmente, contra um PostgreSQL 16 temporário, e CI verde. O cliente HTTP também foi conferido contra o backend real, e os testes novos com mutações. Parte dos PRs do Juan foi integrada sem revisão da Isabela, por decisão do Juan, para adiantar a entrega; a lista completa está na auditoria da Issue #51 |
