package br.com.classbook.desktop.view;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import br.com.classbook.desktop.api.ClienteApi;

/**
 * Controller de {@code tela-principal.fxml} (Issue #55). Cada botão do menu troca o conteúdo de
 * {@code areaConteudo} e destaca o item correspondente; as telas de verdade entram nas Issues #56 a
 * #61, as demais seções seguem com um estado "em construção". A navegação em si não depende de o
 * backend estar no ar.
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
	private void mostrarAutores() {
		mostrarEmConstrucao(botaoAutores, "Autores");
	}

	@FXML
	private void mostrarCategorias() {
		mostrarEmConstrucao(botaoCategorias, "Categorias");
	}

	@FXML
	private void mostrarDisciplinas() {
		mostrarEmConstrucao(botaoDisciplinas, "Disciplinas");
	}

	@FXML
	private void mostrarMateriais() {
		mostrarEmConstrucao(botaoMateriais, "Materiais digitais");
	}

	private void mostrarEmConstrucao(Button botao, String secao) {
		Label icone = new Label("🛠");
		icone.getStyleClass().add("icone");
		Label titulo = new Label(secao);
		titulo.getStyleClass().add("titulo");
		Label descricao = new Label("Esta seção ainda não foi implementada.");
		descricao.getStyleClass().add("descricao");

		VBox estado = new VBox(icone, titulo, descricao);
		estado.getStyleClass().add("estado-vazio");
		estado.setAlignment(Pos.CENTER);

		exibir(botao, estado);
	}

	private void exibir(Button botaoAtivo, Node tela) {
		botoesDoMenu.forEach(botao -> botao.getStyleClass().remove("ativo"));
		botaoAtivo.getStyleClass().add("ativo");
		areaConteudo.getChildren().setAll(tela);
	}

}
