package br.com.classbook.desktop.view;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Predicate;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.AutorResponse;
import br.com.classbook.desktop.modelo.CategoriaResponse;
import br.com.classbook.desktop.modelo.LivroRequest;
import br.com.classbook.desktop.modelo.LivroResponse;

/**
 * Controller de {@code catalogo-livros.fxml} (Issue #56): lista os livros de {@code GET /api/livros}.
 *
 * <p>{@link #carregar(ClienteApi)} dispara a chamada em segundo plano (via {@link EmSegundoPlano}),
 * para a janela não travar enquanto a resposta não chega. Os botões Novo, Editar e Excluir usam o mesmo
 * formulário dos outros cadastros (Issues #58 e #61), e o resultado aparece no aviso do cabeçalho.
 *
 * <p>Busca e filtros (Issue #57): o título, o autor e a categoria disparam {@link #pesquisar()} sozinhos,
 * a cada mudança, com {@link ClienteApi#pesquisarLivros}; os três podem ser combinados. Como cada mudança
 * chama a API de novo, {@link #ultimaPesquisa} garante que só o resultado da pesquisa mais recente chega a
 * aparecer na tabela, mesmo que uma resposta mais antiga volte depois (a chamada roda em segundo plano).
 *
 * <p>Botão Detalhes (Issue #60): abre {@code detalhes-livro.fxml} ({@link DetalhesLivroController}) num
 * diálogo, com todos os dados do livro selecionado e os materiais digitais por disciplina.
 */
public class CatalogoLivrosController {

	private static final String TRACO = "—";

	@FXML
	private Label subtitulo;

	@FXML
	private Label aviso;

	@FXML
	private TextField campoTitulo;

	@FXML
	private ComboBox<AutorResponse> comboAutor;

	@FXML
	private ComboBox<CategoriaResponse> comboCategoria;

	@FXML
	private Button detalhes;

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
	private long ultimaPesquisa;

	/** Pergunta antes de excluir; os testes trocam para não abrir o diálogo. */
	Predicate<String> confirmar = CatalogoLivrosController::confirmarComAlerta;

	/** O último formulário aberto, para os testes. */
	JanelaDeFormulario<LivroRequest, LivroResponse> ultimoFormulario;

	/** O último diálogo de detalhes aberto, e o controller dele, para os testes. */
	Dialog<Void> ultimoDialogoDeDetalhes;
	DetalhesLivroController ultimoControladorDeDetalhes;

	@FXML
	private void initialize() {
		colunaTitulo.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().titulo()));
		colunaAutor.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().autor().nome()));
		colunaCategoria.setCellValueFactory(dados -> new SimpleStringProperty(nomeDaCategoria(dados.getValue())));
		colunaAno.setCellValueFactory(dados -> new SimpleStringProperty(ano(dados.getValue())));
		detalhes.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
		editar.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
		excluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
		tabela.setOnMouseClicked(clique -> {
			if (clique.getClickCount() == 2) {
				editar();
			}
		});
		comboAutor.setConverter(Campos.exibicao(AutorResponse::nome));
		comboCategoria.setConverter(Campos.exibicao(CategoriaResponse::nome));
		campoTitulo.textProperty().addListener((observavel, anterior, atual) -> pesquisar());
		comboAutor.valueProperty().addListener((observavel, anterior, atual) -> pesquisar());
		comboCategoria.valueProperty().addListener((observavel, anterior, atual) -> pesquisar());
	}

	public void carregar(ClienteApi api) {
		this.api = api;
		carregarListasDeFiltro();
		pesquisar();
	}

	private void carregarListasDeFiltro() {
		EmSegundoPlano.executar(() -> new ListasDeFiltro(api.autores().listar(), api.categorias().listar()),
				listas -> {
					comboAutor.getItems().setAll(listas.autores());
					comboCategoria.getItems().setAll(listas.categorias());
				}, falha -> {
					// Os filtros de autor e categoria ficam vazios; a pesquisa por título continua funcionando.
				});
	}

	/** Dispara {@code GET /api/livros} com os filtros atuais; chamado a cada mudança neles. */
	private void pesquisar() {
		long pesquisaAtual = ++ultimaPesquisa;
		String titulo = campoTitulo.getText();
		Long autorId = comboAutor.getValue() == null ? null : comboAutor.getValue().id();
		Long categoriaId = comboCategoria.getValue() == null ? null : comboCategoria.getValue().id();
		EmSegundoPlano.executar(() -> api.pesquisarLivros(titulo, autorId, categoriaId),
				livros -> aplicarSeForAPesquisaAtual(pesquisaAtual, () -> exibir(livros)),
				erro -> aplicarSeForAPesquisaAtual(pesquisaAtual, () -> falhar(erro)));
	}

	/** Só aplica o resultado se nenhuma pesquisa mais nova foi disparada enquanto ele estava a caminho. */
	private void aplicarSeForAPesquisaAtual(long pesquisa, Runnable acao) {
		if (pesquisa == ultimaPesquisa) {
			acao.run();
		}
	}

	@FXML
	void limparFiltros() {
		campoTitulo.clear();
		comboAutor.setValue(null);
		comboCategoria.setValue(null);
	}

	@FXML
	void novo() {
		abrirFormulario(null);
	}

	@FXML
	void detalhes() {
		LivroResponse selecionado = tabela.getSelectionModel().getSelectedItem();
		if (selecionado == null) {
			return;
		}
		FXMLLoader carregador = new FXMLLoader(getClass().getResource("detalhes-livro.fxml"));
		Parent conteudo;
		try {
			conteudo = carregador.load();
		} catch (IOException falha) {
			throw new UncheckedIOException(falha);
		}
		DetalhesLivroController controlador = carregador.getController();
		controlador.preencher(selecionado);
		controlador.carregar(api);

		Dialog<Void> dialogo = new Dialog<>();
		dialogo.setTitle("Detalhes — " + selecionado.titulo());
		dialogo.setResizable(true);
		dialogo.getDialogPane().setContent(conteudo);
		dialogo.getDialogPane().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
		dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		if (tabela.getScene() != null) {
			dialogo.initOwner(tabela.getScene().getWindow());
		}
		ultimoDialogoDeDetalhes = dialogo;
		ultimoControladorDeDetalhes = controlador;
		dialogo.show();
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
		boolean filtrado = temFiltroAtivo();
		mensagemVazia.setText(filtrado ? "Nenhum livro encontrado com esses filtros." : "Nenhum livro cadastrado.");
		subtitulo.setText(contagem(livros.size(), filtrado));
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

	/** Se há algum filtro preenchido (título, autor ou categoria), para a mensagem e a contagem certas. */
	private boolean temFiltroAtivo() {
		return !campoTitulo.getText().isBlank() || comboAutor.getValue() != null || comboCategoria.getValue() != null;
	}

	private static String contagem(int total, boolean filtrado) {
		if (total == 0) {
			return filtrado ? "Nenhum livro encontrado." : "Nenhum livro no acervo.";
		}
		if (filtrado) {
			return total == 1 ? "1 livro encontrado." : total + " livros encontrados.";
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

	private record ListasDeFiltro(List<AutorResponse> autores, List<CategoriaResponse> categorias) {
	}

}
