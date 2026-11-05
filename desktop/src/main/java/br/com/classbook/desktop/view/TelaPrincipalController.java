package br.com.classbook.desktop.view;

import java.io.IOException;
import java.util.List;

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

	@FXML
	private void mostrarCatalogo() throws IOException {
		FXMLLoader carregador = new FXMLLoader(
				getClass().getResource("/br/com/classbook/desktop/view/catalogo-livros.fxml"));
		Parent tela = carregador.load();
		CatalogoLivrosController controlador = carregador.getController();
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
