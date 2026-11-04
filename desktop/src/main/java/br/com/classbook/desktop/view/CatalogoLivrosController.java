package br.com.classbook.desktop.view;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.LivroResponse;

/**
 * Controller de {@code catalogo-livros.fxml} (Issue #56): lista os livros de {@code GET /api/livros}.
 * A busca e os filtros ficam na Issue #57.
 *
 * <p>{@link #carregar(ClienteApi)} dispara a chamada em segundo plano (via {@link EmSegundoPlano}),
 * para a janela não travar enquanto a resposta não chega.
 */
public class CatalogoLivrosController {

	private static final String TRACO = "—";

	@FXML
	private TableView<LivroResponse> tabela;

	@FXML
	private Label mensagemVazia;

	@FXML
	private TableColumn<LivroResponse, String> colunaTitulo;

	@FXML
	private TableColumn<LivroResponse, String> colunaAutor;

	@FXML
	private TableColumn<LivroResponse, String> colunaCategoria;

	@FXML
	private TableColumn<LivroResponse, String> colunaAno;

	@FXML
	private void initialize() {
		colunaTitulo.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().titulo()));
		colunaAutor.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().autor().nome()));
		colunaCategoria.setCellValueFactory(dados -> new SimpleStringProperty(nomeDaCategoria(dados.getValue())));
		colunaAno.setCellValueFactory(
				dados -> new SimpleStringProperty(String.valueOf(dados.getValue().anoPublicacao())));
	}

	public void carregar(ClienteApi api) {
		EmSegundoPlano.executar(() -> api.livros().listar(), this::exibir, this::falhar);
	}

	private void exibir(List<LivroResponse> livros) {
		tabela.getItems().setAll(livros);
		mensagemVazia.setText("Nenhum livro cadastrado.");
	}

	private void falhar(Exception erro) {
		tabela.getItems().clear();
		mensagemVazia.setText(mensagemDeErro(erro));
	}

	static String mensagemDeErro(Exception erro) {
		return erro.getMessage();
	}

	/** O nome da categoria do livro, ou {@value #TRACO} quando o livro não tem categoria. */
	static String nomeDaCategoria(LivroResponse livro) {
		return livro.categoria() == null ? TRACO : livro.categoria().nome();
	}

}
