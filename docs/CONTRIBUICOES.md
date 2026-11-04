# Contribuições individuais

Registro das contribuições de cada integrante. **Preencha somente com o que realmente aconteceu**, a cada PR integrado. Os números das Issues e dos PRs permitem conferir tudo no GitHub (commits, revisões e Actions).

## Resumo por integrante

| Integrante | Issues realizadas | Pull requests | Funcionalidades implementadas | Testes executados |
| --- | --- | --- | --- | --- |
| Isabela Ohana (@isabelaohanadev) | — | — | — | — |
| Juan Ribeiro (@JuanRiber) | — | — | — | — |

## Detalhamento por pull request

| PR | Autor | Issue(s) | O que mudou | Revisor | Testes executados |
| --- | --- | --- | --- | --- | --- |
| — | — | — | — | — | — |

## Registro de uso de ferramentas de IA

Toda ferramenta de IA usada no projeto deve ser registrada aqui, com o que foi feito e como o resultado foi revisado (Issue #65).

| Data | Ferramenta | Quem usou | Para quê | Revisão humana |
| --- | --- | --- | --- | --- |
| 31/10/2026 | Claude Code | Isabela Ohana | Implementar o CRUD de Autor e de Livro da Etapa 2 (Issues #11–#25 e #27: entidades, DTOs, repositories, services, controllers, relacionamento Livro–Autor e pesquisa por título), seguindo as convenções de `docs/ARQUITETURA.md` e `docs/MODELAGEM.md` | Isabela acompanhou e direcionou a execução nesta sessão; a suíte de testes completa (`./mvnw verify`) rodou localmente contra um PostgreSQL 16, com resultado verde, antes de cada um dos 16 PRs (#101–#116). A revisão cruzada do Juan foi feita em 31/10/2026: os PRs foram aprovados, com comentários (ver a linha seguinte) |
| 31/10 a 01/11/2026 | Claude Code | Juan Ribeiro | Revisar os PRs #101–#118 da Isabela; implementar a Etapa 3 (Issues #31–#41: Categoria, Disciplina e MaterialDigital, relacionamento Material–Disciplina, validação manual das rotas e testes das rotas e validações) e as Issues #42, #43, #44 e #47 (validações de entrada, tratador global de exceções, formato de erro e Swagger), seguindo `docs/ARQUITETURA.md` e `docs/MODELAGEM.md` | Juan definiu as tarefas e acompanhou a execução nesta sessão. A suíte completa (`./mvnw verify`) passou antes de cada PR: no CI no #119 e localmente, contra um PostgreSQL 16, do #121 ao #132. Os testes de relacionamento e de validação foram conferidos com mutações (um defeito introduzido de propósito para ver o teste falhar), e as rotas da Etapa 3 foram validadas manualmente na #40. A revisão cruzada da Isabela está pendente nesses PRs |
| 01/11 a 04/11/2026 | Claude Code | Isabela Ohana | Registrar a interface JavaFX como opcional e pós-avaliação (Issue #93, PR #98); criar a estrutura do módulo `desktop/` e a tela principal com navegação (Issues #54 e #55, PRs #137 e #138); configurar o relacionamento Livro–Categoria e os filtros de pesquisa por autor e categoria (Issues #26 e #28, PRs #140 e #142); tratar a duplicidade de ISBN e a exclusão de categoria em uso (Issues #46 e #45, PRs #136 e #141); consolidar os testes de filtro (Issue #29, PR #143); documentar no Swagger as rotas de Autor e Livro (Issue #48, PR #144); corrigir uma regressão em `LivroRequest` após o merge do #140 (PR #145); registrar a data da apresentação na Issue #94 (PR #118); e, numa sessão posterior, fechar as Issues #71 e #73 (já validadas pelo Juan em comentário) e preencher as contribuições individuais da Issue #64 (PR #150) | Isabela pediu a conclusão das Issues pendentes atribuídas a ela e a abertura de um PR por Issue, seguindo o `CONTRIBUTING.md`; decisões que afetam o repositório compartilhado (permissões de escrita, configuração da proteção da `main`) foram confirmadas com ela antes de executar. A suíte completa (`./mvnw verify`) rodou localmente contra um PostgreSQL 16 e no CI antes de cada PR com mudança de código, com resultado verde; PRs somente de documentação (como este) não exigem banco. A revisão cruzada do Juan está pendente nesses PRs |
