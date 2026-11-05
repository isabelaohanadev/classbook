package br.com.classbook.desktop.view;

import java.util.List;
import java.util.function.Predicate;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.LivroRequest;
import br.com.classbook.desktop.modelo.LivroResponse;

/**
 * Controller de {@code catalogo-livros.fxml} (Issue #56): lista os livros de {@code GET /api/livros}.
 * A busca e os filtros ficam na Issue #57.
 *
 * <p>{@link #carregar(ClienteApi)} dispara a chamada em segundo plano (via {@link EmSegundoPlano}),
 * para a janela não travar enquanto a resposta não chega. Os botões Novo, Editar e Excluir usam o mesmo
 * formulário dos outros cadastros (Issues #58 e #61), e o resultado aparece no aviso do cabeçalho.
 */
public class CatalogoLivrosController {

	private static final String TRACO = "—";

	@FXML
	private Label subtitulo;

	@FXML
	private Label aviso;

	@FXML
	private Button editar;

	@FXML
	private Button excluir;

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

	private final Cadastro<LivroRequest, LivroResponse> cadastro = Cadastro.livros();
	private ClienteApi api;

	/** Pergunta antes de excluir; os testes trocam para não abrir o diálogo. */
	Predicate<String> confirmar = CatalogoLivrosController::confirmarComAlerta;

	/** O último formulário aberto, para os testes. */
	JanelaDeFormulario<LivroRequest, LivroResponse> ultimoFormulario;

	@FXML
	private void initialize() {
		colunaTitulo.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().titulo()));
		colunaAutor.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().autor().nome()));
		colunaCategoria.setCellValueFactory(dados -> new SimpleStringProperty(nomeDaCategoria(dados.getValue())));
		colunaAno.setCellValueFactory(dados -> new SimpleStringProperty(ano(dados.getValue())));
		editar.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
		excluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
		tabela.setOnMouseClicked(clique -> {
			if (clique.getClickCount() == 2) {
				editar();
			}
		});
	}

	public void carregar(ClienteApi api) {
		this.api = api;
		EmSegundoPlano.executar(() -> api.livros().listar(), this::exibir, this::falhar);
	}

	@FXML
	void novo() {
		abrirFormulario(null);
	}

	@FXML
	void editar() {
		LivroResponse selecionado = tabela.getSelectionModel().getSelectedItem();
		if (selecionado != null) {
			abrirFormulario(selecionado);
		}
	}

	@FXML
	void excluir() {
		LivroResponse selecionado = tabela.getSelectionModel().getSelectedItem();
		if (selecionado == null || !confirmar.test(cadastro.nome().perguntaDeExclusao(selecionado.titulo()))) {
			return;
		}
		EmSegundoPlano.executar(() -> {
			api.livros().excluir(selecionado.id());
			return selecionado.id();
		}, excluido -> {
			Mensagens.sucesso(aviso, cadastro.nome().excluido());
			carregar(api);
		}, falha -> Mensagens.erro(aviso, Mensagens.deErro(falha)));
	}

	@FXML
	void atualizar() {
		aviso.setText("");
		carregar(api);
	}

	private void abrirFormulario(LivroResponse livro) {
		ultimoFormulario = new JanelaDeFormulario<>(cadastro, api, livro, salvo -> {
			Mensagens.sucesso(aviso, livro == null ? cadastro.nome().cadastrado() : cadastro.nome().atualizado());
			carregar(api);
		});
		ultimoFormulario.abrir(tabela.getScene() == null ? null : tabela.getScene().getWindow());
	}

	private void exibir(List<LivroResponse> livros) {
		tabela.getItems().setAll(livros);
		aplicarEstiloDaMensagem(false);
		mensagemVazia.setText("Nenhum livro cadastrado.");
		subtitulo.setText(contagem(livros.size()));
	}

	private void falhar(Exception erro) {
		tabela.getItems().clear();
		aplicarEstiloDaMensagem(true);
		mensagemVazia.setText(mensagemDeErro(erro));
		subtitulo.setText("Não foi possível carregar o acervo.");
	}

	private void aplicarEstiloDaMensagem(boolean erro) {
		mensagemVazia.getStyleClass().removeAll("mensagem-vazia", "mensagem-erro");
		mensagemVazia.getStyleClass().add(erro ? "mensagem-erro" : "mensagem-vazia");
	}

	private static String contagem(int total) {
		if (total == 0) {
			return "Nenhum livro no acervo.";
		}
		return total == 1 ? "1 livro no acervo." : total + " livros no acervo.";
	}

	static String mensagemDeErro(Exception erro) {
		return Mensagens.deErro(erro);
	}

	/** O ano de publicação, ou {@value #TRACO} quando o livro não tem ano (o campo é opcional). */
	static String ano(LivroResponse livro) {
		return livro.anoPublicacao() == null ? TRACO : livro.anoPublicacao().toString();
	}

	/** O nome da categoria do livro, ou {@value #TRACO} quando o livro não tem categoria. */
	static String nomeDaCategoria(LivroResponse livro) {
		return livro.categoria() == null ? TRACO : livro.categoria().nome();
	}

	private static boolean confirmarComAlerta(String pergunta) {
		Alert alerta = new Alert(AlertType.CONFIRMATION, pergunta, ButtonType.YES, ButtonType.NO);
		alerta.setHeaderText(null);
		return alerta.showAndWait().filter(ButtonType.YES::equals).isPresent();
	}

}
