# ClassBook Desktop (JavaFX)

Interface gráfica desktop do ClassBook. Ela **consome a API REST** do backend e não acessa o banco de dados diretamente.

## Estrutura (Issue #54)

Projeto Maven **independente** do backend: `desktop/pom.xml` próprio, com `groupId`/`artifactId` só dele (`br.com.classbook:classbook-desktop`). A raiz não lista `desktop` em `<modules>` nem depende dele; `./mvnw -B verify` rodado na raiz do repositório ignora esta pasta.

```text
desktop/
├── pom.xml                 # projeto Maven próprio (Java 21, JavaFX 21)
├── mvnw, mvnw.cmd, .mvn/   # wrapper do Maven, igual ao da raiz
├── src/main/
│   ├── java/br/com/classbook/desktop/
│   │   ├── app/            # ponto de entrada (Application)
│   │   ├── api/            # cliente HTTP da API REST (Issue #59)
│   │   ├── modelo/         # Request e Response da API, espelhando os DTOs do backend
│   │   └── view/           # controllers das telas
│   └── resources/br/com/classbook/desktop/view/
│       └── *.fxml          # FXML de cada tela, ao lado do controller correspondente
└── src/test/java/          # testes do cliente HTTP e das telas, sem abrir janela
```

- **FXML:** cada tela tem um `.fxml` (em `resources`, no mesmo pacote do controller) carregado por `FXMLLoader`. Sem Scene Builder nem `module-info.java`: o projeto roda em classpath simples (sem JPMS), para manter o setup enxuto.
- **Navegação:** a tela principal (Issue #55) usa um `BorderPane` raiz — menu lateral à esquerda, conteúdo no centro — e troca o `center` ao clicar em cada seção. Sem roteador à parte: é pouca coisa para justificar uma abstração própria.
- **Cliente HTTP (Issue #59):** o `ClienteApi` (pacote `api`) usa o `java.net.http.HttpClient` com Jackson. Para cada recurso, ele tem as cinco operações (`api.autores().listar()`, `buscar(id)`, `cadastrar(request)`, `editar(id, request)` e `excluir(id)`), e para os livros tem também `pesquisarLivros(titulo, autorId, categoriaId)`. Os `Request` e `Response` ficam no pacote `modelo`, com os mesmos campos dos DTOs do backend.
- **Erros da API:** uma resposta `4xx` ou `5xx` vira `ErroDaApiException`, com o código e a mensagem que a API mandou, seguida dos campos inválidos. Se a API não responde (backend fora do ar ou URL errada), o cliente lança `ApiIndisponivelException`, com uma mensagem que pode ir direto para a tela.
- **Fora da thread da interface:** as chamadas do `ClienteApi` esperam a resposta, então as telas as fazem por `EmSegundoPlano` (pacote `view`). Ele roda a chamada numa thread separada e devolve o resultado, ou o erro, na thread do JavaFX:

  ```java
  EmSegundoPlano.executar(() -> api.autores().listar(), tabela.getItems()::setAll, this::mostrarErro);
  ```

## Como compilar e rodar

```bash
cd desktop
./mvnw -B compile      # só compila
./mvnw -B test         # roda os testes do cliente HTTP e das telas (não abre janela nem precisa do backend)
./mvnw javafx:run      # compila e abre a aplicação
```

Primeira execução baixa o Maven, o JavaFX e o plugin (`org.openjfx:javafx-maven-plugin`); pode levar alguns minutos.

A aplicação chama a API em `http://localhost:8080/api`, então suba o backend antes (veja o README da raiz). Para usar outro endereço, defina a variável de ambiente `CLASSBOOK_API_URL`:

```bash
CLASSBOOK_API_URL=http://192.168.0.10:8080/api ./mvnw javafx:run
```

Se a propriedade de sistema `classbook.api.url` estiver definida na JVM da aplicação, ela tem prioridade sobre a variável.

## Integração contínua (CI)

**O módulo `desktop` não entra no CI.** O workflow em `.github/workflows/ci.yml` continua rodando só `mvn -B verify` na raiz (o backend Spring Boot). Testar uma aplicação JavaFX de forma automática exigiria um display virtual (Xvfb ou similar) e testes de UI, o que foge do escopo acadêmico ("manter simples", ver `docs/REQUISITOS.md`). A verificação deste módulo é manual: compilar e abrir a aplicação antes de abrir o PR. Rode também o `./mvnw -B test`. Os testes usam um servidor HTTP local no lugar da API. Os das telas rodam o JavaFX sem janela (Monocle Headless, configurado no `pom.xml`), então nenhum precisa de display nem do backend.

## Implementado

- Tela principal com navegação simples entre catálogo, autores, categorias, disciplinas e materiais
  (Issue #55). A navegação em si não depende do backend: cada tela carrega os dados em segundo plano.
- Cliente HTTP dos cinco recursos e da pesquisa de livros, com os erros da API convertidos em exceções
  e as chamadas fora da thread da interface (Issue #59).
- Catálogo de livros: lista título, autor, categoria e ano com `GET /api/livros`, chamado por
  `EmSegundoPlano` (Issue #56). Lista vazia ou erro da API mostram uma mensagem no lugar da tabela.
  Ainda sem busca nem filtros (Issue #57). Os botões Novo livro, Editar e Excluir usam o formulário de
  livro (Issue #58), e um livro sem categoria ou sem ano mostra "—".
- Identidade visual: paleta de cores, tipografia e a marca ClassBook na barra lateral, em
  `styles.css`. Item ativo do menu destacado, estados de carregando/vazio/erro do catálogo
  diferenciados visualmente, e placeholder "em construção" nas seções ainda não implementadas.
  Polimento das Issues #55 e #56, sem Issue própria.
- Cadastro e edição dos cinco recursos (Issue #58). Autores, Categorias, Disciplinas e Materiais usam a
  mesma `tela-de-cadastro.fxml` (`TelaDeCadastroController`), configurada em `Cadastro` com as colunas,
  a rota e o formulário de cada recurso. Os livros usam o catálogo. Cada formulário é um FXML
  (`formulario-*.fxml`) aberto pela `JanelaDeFormulario`:
  - Os campos obrigatórios têm `*` e ficam marcados se estiverem vazios, sem chamar a API.
  - As listas de autor, categoria e disciplina vêm da API.
  - Cancelar descarta tudo sem chamar a API.
- Mensagens de sucesso e de erro (Issue #61):
  - Cadastro, edição e exclusão bem-sucedidos mostram o aviso no cabeçalho ("Disciplina cadastrada.").
  - Os erros `400`, `404` e `409` mostram a mensagem da API: no formulário, que continua aberto com o que
    foi digitado, ou no aviso.
  - Com o backend fora do ar, aparece "Não foi possível falar com a API em ...". Um erro inesperado
    mostra uma mensagem genérica, sem stack trace (`Mensagens`).

## Planejado (Issues #57 e #60)

- Busca e filtros no catálogo de livros (Issue #57).
- Detalhes do livro e links dos materiais digitais (Issue #60).
- Sem telas de login nem dashboards.

## Divisão inicial

- **Isabela:** tela principal, navegação e catálogo.
- **Juan:** cliente HTTP, formulários de cadastro e edição.
