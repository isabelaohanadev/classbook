# ClassBook Desktop (JavaFX)

Interface gráfica desktop do ClassBook. Ela **consome a API REST** do backend e não acessa o banco de dados diretamente.

## Estrutura (Issue #54)

Projeto Maven **independente** do backend: `desktop/pom.xml` próprio, com `groupId`/`artifactId` só dele (`br.com.classbook:classbook-desktop`). A raiz não lista `desktop` em `<modules>` nem depende dele; `./mvnw -B verify` rodado na raiz do repositório ignora esta pasta.

```text
desktop/
├── pom.xml                 # projeto Maven próprio (Java 21, JavaFX 21)
├── mvnw, mvnw.cmd, .mvn/   # wrapper do Maven, igual ao da raiz
└── src/main/
    ├── java/br/com/classbook/desktop/
    │   ├── app/            # ponto de entrada (Application)
    │   └── view/           # controllers das telas
    └── resources/br/com/classbook/desktop/view/
        └── *.fxml          # FXML de cada tela, ao lado do controller correspondente
```

- **FXML:** cada tela tem um `.fxml` (em `resources`, no mesmo pacote do controller) carregado por `FXMLLoader`. Sem Scene Builder nem `module-info.java`: o projeto roda em classpath simples (sem JPMS), para manter o setup enxuto.
- **Navegação:** a tela principal (Issue #55) usa um `BorderPane` raiz — menu lateral à esquerda, conteúdo no centro — e troca o `center` ao clicar em cada seção. Sem roteador à parte: é pouca coisa para justificar uma abstração própria.
- **Cliente HTTP (Issue #59, responsabilidade do Juan):** `java.net.http.HttpClient` com Jackson para (des)serializar `Request`/`Response`, chamando a API em `http://localhost:8080/api` (URL base configurável). Nenhuma classe desse cliente existe ainda neste módulo — entra só na Issue #59, para o pacote `view` não ficar esperando uma dependência que ainda não existe.

## Como compilar e rodar

```bash
cd desktop
./mvnw -B compile      # só compila
./mvnw javafx:run      # compila e abre a aplicação
```

Primeira execução baixa o Maven, o JavaFX e o plugin (`org.openjfx:javafx-maven-plugin`); pode levar alguns minutos.

## Integração contínua (CI)

**O módulo `desktop` não entra no CI.** O workflow em `.github/workflows/ci.yml` continua rodando só `mvn -B verify` na raiz (o backend Spring Boot). Testar uma aplicação JavaFX de forma automática exigiria um display virtual (Xvfb ou similar) e testes de UI, o que foge do escopo acadêmico ("manter simples", ver `docs/REQUISITOS.md`). A verificação deste módulo é manual: compilar e abrir a aplicação antes de abrir o PR.

## Implementado

- Tela principal com navegação simples entre catálogo, autores, categorias, disciplinas e materiais
  (Issue #55). Cada seção ainda mostra um texto de espaço reservado — as telas de verdade entram nas
  próximas Issues. A navegação não faz nenhuma chamada HTTP, então funciona com o backend fora do ar.

## Planejado (Issues #56 a #61)

- Catálogo de livros com busca e filtros (Issues #56 e #57).
- Formulários de cadastro e edição dos cinco recursos (Issue #58).
- Detalhes do livro e links dos materiais digitais (Issue #60).
- Mensagens de sucesso e de erro (Issue #61).
- Sem telas de login nem dashboards.

## Divisão inicial

- **Isabela:** tela principal, navegação e catálogo.
- **Juan:** cliente HTTP, formulários de cadastro e edição.
