# ClassBook Desktop (JavaFX)

Interface gráfica desktop do ClassBook. Ela **consome a API REST** do backend e não acessa o banco de dados diretamente.

> **Ainda não implementado.** Esta pasta reserva o lugar do módulo JavaFX. A estrutura da aplicação (projeto Maven próprio, pacotes e navegação) será definida na Issue #54; as telas ficam nas Issues #55 a #61.

## Planejado

- Projeto independente do backend, com `pom.xml` próprio nesta pasta (o build da raiz não compila o JavaFX).
- Comunicação HTTP com a API em `http://localhost:8080/api` (Issue #59).
- Telas simples: principal com navegação, catálogo de livros com busca e filtros, formulários de cadastro e edição, detalhes de livro e links dos materiais digitais, mensagens de sucesso e erro.
- Sem telas de login nem dashboards.

## Divisão inicial

- **Isabela:** tela principal, navegação e catálogo.
- **Juan:** formulários de cadastro e edição.
