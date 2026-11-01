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
