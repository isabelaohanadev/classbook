package br.com.classbook.desktop.view;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import br.com.classbook.desktop.api.ClienteApi;

/**
 * Controller de {@code tela-principal.fxml} (Issue #55). Cada botão do menu troca o conteúdo do
 * {@code center}; as telas de verdade entram nas Issues #56 a #61, as demais seções seguem como
 * texto provisório. A navegação em si não depende de o backend estar no ar.
 */
public class TelaPrincipalController {

	private final ClienteApi api = new ClienteApi();

	@FXML
	private BorderPane raiz;

	@FXML
	private Label conteudo;

	@FXML
	private void mostrarCatalogo() throws IOException {
		FXMLLoader carregador = new FXMLLoader(
				getClass().getResource("/br/com/classbook/desktop/view/catalogo-livros.fxml"));
		Parent tela = carregador.load();
		CatalogoLivrosController controlador = carregador.getController();
		controlador.carregar(api);
		raiz.setCenter(tela);
	}

	@FXML
	private void mostrarAutores() {
		mostrar("Autores");
	}

	@FXML
	private void mostrarCategorias() {
		mostrar("Categorias");
	}

	@FXML
	private void mostrarDisciplinas() {
		mostrar("Disciplinas");
	}

	@FXML
	private void mostrarMateriais() {
		mostrar("Materiais digitais");
	}

	private void mostrar(String secao) {
		raiz.setCenter(conteudo);
		conteudo.setText(secao + " (em construção)");
	}

}
