package br.com.classbook.desktop.view;

import static br.com.classbook.desktop.view.Fx.esperarAte;
import static br.com.classbook.desktop.view.Fx.naThread;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.classbook.desktop.api.ApiFalsa;
import br.com.classbook.desktop.api.ApiIndisponivelException;
import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.api.ErroDaApiException;
import br.com.classbook.desktop.modelo.AutorResponse;
import br.com.classbook.desktop.modelo.CategoriaResponse;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.LivroResponse.AutorResumo;
import br.com.classbook.desktop.modelo.LivroResponse.CategoriaResumo;

/**
 * {@code catalogo-livros.fxml}: os métodos estáticos de formatação e, com a tela de verdade, a busca por
 * título e os filtros de autor e categoria da Issue #57 — sozinhos e combinados — contra a {@link ApiFalsa}.
 */
class CatalogoLivrosControllerTests {

	private static final String DOIS_AUTORES = """
			[{"id": 1, "nome": "Machado de Assis", "nacionalidade": "Brasileira", "dataNascimento": "1839-06-21"},
			 {"id": 2, "nome": "Clarice Lispector", "nacionalidade": "Brasileira", "dataNascimento": "1920-12-10"}]""";

	private static final String DUAS_CATEGORIAS = """
			[{"id": 1, "nome": "Literatura", "descricao": null, "faixaEtaria": null, "tipo": "LITERATURA"},
			 {"id": 2, "nome": "Ficção", "descricao": null, "faixaEtaria": null, "tipo": "LITERATURA"}]""";

	private static final String TRES_LIVROS = """
			[{"id": 1, "titulo": "Dom Casmurro", "isbn": "978-85-359-0277-5", "anoPublicacao": 1899,
			  "descricao": null, "editora": null, "autor": {"id": 1, "nome": "Machado de Assis"},
			  "categoria": {"id": 1, "nome": "Literatura"}},
			 {"id": 2, "titulo": "Memórias Póstumas de Brás Cubas", "isbn": "978-85-359-0278-2",
			  "anoPublicacao": 1881, "descricao": null, "editora": null,
			  "autor": {"id": 1, "nome": "Machado de Assis"}, "categoria": {"id": 1, "nome": "Literatura"}},
			 {"id": 3, "titulo": "A Hora da Estrela", "isbn": "978-85-325-0812-0", "anoPublicacao": 1977,
			  "descricao": null, "editora": null, "autor": {"id": 2, "nome": "Clarice Lispector"},
			  "categoria": {"id": 2, "nome": "Ficção"}}]""";

	private static final String UM_LIVRO = """
			[{"id": 1, "titulo": "Dom Casmurro", "isbn": "978-85-359-0277-5", "anoPublicacao": 1899,
			  "descricao": null, "editora": null, "autor": {"id": 1, "nome": "Machado de Assis"},
			  "categoria": {"id": 1, "nome": "Literatura"}}]""";

	private ApiFalsa api;
	private Parent tela;
	private CatalogoLivrosController controlador;

	@BeforeAll
	static void iniciarJavaFx() throws InterruptedException {
		Fx.iniciar();
	}

	@BeforeEach
	void subirApiFalsa() throws IOException {
		api = new ApiFalsa();
		api.responder("GET /api/autores", 200, DOIS_AUTORES);
		api.responder("GET /api/categorias", 200, DUAS_CATEGORIAS);
	}

	@AfterEach
	void derrubarApiFalsa() {
		api.close();
	}

	@Test
	void nomeDaCategoriaUsaUmTracoQuandoOLivroNaoTemCategoria() {
		assertThat(CatalogoLivrosController.nomeDaCategoria(livro(null))).isEqualTo("—");
		assertThat(CatalogoLivrosController.nomeDaCategoria(livro(new CategoriaResumo(2L, "Ficção"))))
				.isEqualTo("Ficção");
	}

	@Test
	void anoUsaUmTracoQuandoOLivroNaoTemAno() {
		LivroResponse semAno = new LivroResponse(2L, "A Hora da Estrela", "9788532508120", null, null, null,
				new AutorResumo(2L, "Clarice Lispector"), null);

		assertThat(CatalogoLivrosController.ano(livro(null))).isEqualTo("1899");
		assertThat(CatalogoLivrosController.ano(semAno)).isEqualTo("—");
	}

	@Test
	void mensagemDeErroUsaAMensagemDaExcecao() {
		ErroDaApiException erroDaApi = new ErroDaApiException(500, "A API respondeu com o código 500.", List.of());
		ApiIndisponivelException apiIndisponivel =
				new ApiIndisponivelException("http://localhost:8080/api", new IOException("conexão recusada"));

		assertThat(CatalogoLivrosController.mensagemDeErro(erroDaApi)).isEqualTo(erroDaApi.getMessage());
		assertThat(CatalogoLivrosController.mensagemDeErro(apiIndisponivel)).isEqualTo(apiIndisponivel.getMessage());
	}

	@Test
	void carregarPreencheOsCombosDeAutorECategoriaComAApi() throws Exception {
		api.responder("GET /api/livros", 200, TRES_LIVROS);
		abrir(new ClienteApi(api.url()));

		esperarAte("os autores chegarem", () -> !comboAutor().getItems().isEmpty());
		assertThat(naThread(() -> comboAutor().getItems().stream().map(AutorResponse::nome).toList()))
				.containsExactly("Machado de Assis", "Clarice Lispector");
		assertThat(naThread(() -> comboCategoria().getItems().stream().map(CategoriaResponse::nome).toList()))
				.containsExactly("Literatura", "Ficção");
	}

	@Test
	void buscarPorTituloMandaOFiltroEMostraOResultado() throws Exception {
		api.responder("GET /api/livros", 200, TRES_LIVROS);
		abrir(new ClienteApi(api.url()));
		esperarAte("os três livros chegarem", () -> tabela().getItems().size() == 3);

		api.responder("GET /api/livros", 200, UM_LIVRO);
		naThread(() -> {
			campoTitulo().setText("Dom");
			return null;
		});

		esperarAte("a pesquisa filtrar", () -> tabela().getItems().size() == 1);
		assertThat(naThread(() -> tabela().getItems().getFirst().titulo())).isEqualTo("Dom Casmurro");
		assertThat(ultimaConsultaEm("GET /api/livros")).isEqualTo("titulo=Dom");
	}

	@Test
	void filtrosDeAutorECategoriaSaoCombinaveisComOTitulo() throws Exception {
		api.responder("GET /api/livros", 200, TRES_LIVROS);
		abrir(new ClienteApi(api.url()));
		esperarAte("os três livros chegarem", () -> tabela().getItems().size() == 3);
		esperarAte("os autores chegarem", () -> !comboAutor().getItems().isEmpty());

		naThread(() -> {
			comboAutor().setValue(comboAutor().getItems().get(0));
			return null;
		});
		esperarAte("o filtro de autor chamar a API", () -> "autorId=1".equals(ultimaConsultaEm("GET /api/livros")));

		naThread(() -> {
			comboCategoria().setValue(comboCategoria().getItems().get(0));
			return null;
		});
		esperarAte("autor e categoria combinados chamarem a API",
				() -> "autorId=1&categoriaId=1".equals(ultimaConsultaEm("GET /api/livros")));

		naThread(() -> {
			campoTitulo().setText("Dom");
			return null;
		});
		esperarAte("o título entrar na combinação",
				() -> "titulo=Dom&autorId=1&categoriaId=1".equals(ultimaConsultaEm("GET /api/livros")));
	}

	@Test
	void limparFiltrosVoltaAMostrarTodosOsLivros() throws Exception {
		api.responder("GET /api/livros", 200, UM_LIVRO);
		abrir(new ClienteApi(api.url()));
		esperarAte("o filtro inicial carregar", () -> tabela().getItems().size() == 1);
		esperarAte("os autores chegarem", () -> !comboAutor().getItems().isEmpty());
		naThread(() -> {
			campoTitulo().setText("Dom");
			comboAutor().setValue(comboAutor().getItems().get(0));
			return null;
		});
		esperarAte("o filtro combinado ser aplicado",
				() -> "titulo=Dom&autorId=1".equals(ultimaConsultaEm("GET /api/livros")));

		api.responder("GET /api/livros", 200, TRES_LIVROS);
		naThread(() -> {
			controlador.limparFiltros();
			return null;
		});

		esperarAte("a lista completa voltar", () -> tabela().getItems().size() == 3);
		assertThat(naThread(() -> campoTitulo().getText())).isEmpty();
		assertThat(naThread(comboAutor()::getValue)).isNull();
		assertThat(naThread(comboCategoria()::getValue)).isNull();
	}

	@Test
	void semResultadosNaPesquisaMostraMensagemDiferenteDeAcervoVazio() throws Exception {
		api.responder("GET /api/livros", 200, TRES_LIVROS);
		abrir(new ClienteApi(api.url()));
		esperarAte("os três livros chegarem", () -> tabela().getItems().size() == 3);

		api.responder("GET /api/livros", 200, "[]");
		naThread(() -> {
			campoTitulo().setText("Não existe");
			return null;
		});

		esperarAte("a mensagem de nenhum resultado",
				() -> placeholder().getText().equals("Nenhum livro encontrado com esses filtros."));
		assertThat(naThread(() -> texto("#subtitulo"))).isEqualTo("Nenhum livro encontrado.");
	}

	private void abrir(ClienteApi cliente) throws Exception {
		naThread(() -> {
			FXMLLoader carregador =
					new FXMLLoader(CatalogoLivrosController.class.getResource("catalogo-livros.fxml"));
			tela = carregador.load();
			controlador = carregador.getController();
			Stage palco = new Stage();
			palco.setScene(new Scene(tela));
			palco.show();
			controlador.carregar(cliente);
			return null;
		});
	}

	/** A query string (sem o {@code ?}) da última requisição recebida numa rota, ou {@code null}. */
	private String ultimaConsultaEm(String rota) {
		List<ApiFalsa.Requisicao> daRota =
				api.recebidas().stream().filter(requisicao -> requisicao.rota().equals(rota)).toList();
		return daRota.isEmpty() ? null : daRota.getLast().consulta();
	}

	@SuppressWarnings("unchecked")
	private TableView<LivroResponse> tabela() {
		return (TableView<LivroResponse>) tela.lookup("#tabela");
	}

	private Label placeholder() {
		return (Label) tabela().getPlaceholder();
	}

	private TextField campoTitulo() {
		return (TextField) tela.lookup("#campoTitulo");
	}

	@SuppressWarnings("unchecked")
	private ComboBox<AutorResponse> comboAutor() {
		return (ComboBox<AutorResponse>) tela.lookup("#comboAutor");
	}

	@SuppressWarnings("unchecked")
	private ComboBox<CategoriaResponse> comboCategoria() {
		return (ComboBox<CategoriaResponse>) tela.lookup("#comboCategoria");
	}

	private String texto(String seletor) {
		return ((Label) tela.lookup(seletor)).getText();
	}

	private static LivroResponse livro(CategoriaResumo categoria) {
		return new LivroResponse(1L, "Dom Casmurro", "978-85-359-0277-5", 1899, null, null,
				new AutorResumo(1L, "Machado de Assis"), categoria);
	}

}
