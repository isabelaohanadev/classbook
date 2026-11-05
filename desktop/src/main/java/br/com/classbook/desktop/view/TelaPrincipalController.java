package br.com.classbook.desktop.view;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import br.com.classbook.desktop.api.ClienteApi;

/**
 * Controller de {@code tela-principal.fxml} (Issue #55). Cada botão do menu troca o conteúdo de
 * {@code areaConteudo} e destaca o item correspondente: o catálogo de livros (Issue #56) e as telas de
 * cadastro de autores, categorias, disciplinas e materiais (Issue #58). A navegação em si não depende de o
 * backend estar no ar; cada tela carrega os dados da API em segundo plano.
 */
public class TelaPrincipalController {

	private final ClienteApi api = new ClienteApi();

	/** Como abrir um link no navegador padrão; vale para as telas que têm links (detalhes do livro). */
	private Consumer<URI> abrirNoNavegador = DetalhesLivroController.SEM_NAVEGADOR;

	@FXML
	private StackPane areaConteudo;

	@FXML
	private Button botaoCatalogo;

	@FXML
	private Button botaoAutores;

	@FXML
	private Button botaoCategorias;

	@FXML
	private Button botaoDisciplinas;

	@FXML
	private Button botaoMateriais;

	private List<Button> botoesDoMenu;

	@FXML
	private void initialize() {
		botoesDoMenu = List.of(botaoCatalogo, botaoAutores, botaoCategorias, botaoDisciplinas, botaoMateriais);
	}

	/**
	 * Liga os links das telas ao navegador padrão pelo {@code HostServices} do JavaFX, que só existe depois
	 * que a aplicação inicia: a {@code AplicacaoDesktop} chama isto logo depois de carregar a tela. O
	 * {@code HostServices} abre pelo próprio toolkit do JavaFX, sem subir o AWT ({@code java.awt.Desktop})
	 * dentro de uma aplicação JavaFX.
	 */
	public void configurar(HostServices hostServices) {
		abrirNoNavegador = uri -> hostServices.showDocument(uri.toString());
	}

	@FXML
	private void mostrarCatalogo() throws IOException {
		FXMLLoader carregador = new FXMLLoader(
				getClass().getResource("/br/com/classbook/desktop/view/catalogo-livros.fxml"));
		Parent tela = carregador.load();
		CatalogoLivrosController controlador = carregador.getController();
		controlador.abrirNoNavegador = abrirNoNavegador;
		controlador.carregar(api);
		exibir(botaoCatalogo, tela);
	}

	@FXML
	private void mostrarAutores() throws IOException {
		mostrarCadastro(botaoAutores, Cadastro.autores());
	}

	@FXML
	private void mostrarCategorias() throws IOException {
		mostrarCadastro(botaoCategorias, Cadastro.categorias());
	}

	@FXML
	private void mostrarDisciplinas() throws IOException {
		mostrarCadastro(botaoDisciplinas, Cadastro.disciplinas());
	}

	@FXML
	private void mostrarMateriais() throws IOException {
		mostrarCadastro(botaoMateriais, Cadastro.materiais());
	}

	private <Q, R> void mostrarCadastro(Button botao, Cadastro<Q, R> cadastro) throws IOException {
		FXMLLoader carregador = new FXMLLoader(
				getClass().getResource("/br/com/classbook/desktop/view/tela-de-cadastro.fxml"));
		Parent tela = carregador.load();
		TelaDeCadastroController<Q, R> controlador = carregador.getController();
		controlador.configurar(cadastro, api);
		exibir(botao, tela);
	}

	private void exibir(Button botaoAtivo, Node tela) {
		botoesDoMenu.forEach(botao -> botao.getStyleClass().remove("ativo"));
		botaoAtivo.getStyleClass().add("ativo");
		areaConteudo.getChildren().setAll(tela);
	}

}
