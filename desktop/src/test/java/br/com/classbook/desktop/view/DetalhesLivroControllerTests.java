package br.com.classbook.desktop.view;

import static br.com.classbook.desktop.view.Fx.esperarAte;
import static br.com.classbook.desktop.view.Fx.naThread;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.classbook.desktop.api.ApiFalsa;
import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.FormatoMaterial;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.LivroResponse.AutorResumo;
import br.com.classbook.desktop.modelo.LivroResponse.CategoriaResumo;
import br.com.classbook.desktop.modelo.MaterialDigitalResponse;
import br.com.classbook.desktop.modelo.MaterialDigitalResponse.DisciplinaResumo;

/**
 * {@code detalhes-livro.fxml} (Issue #60): todos os dados do livro, os materiais digitais por disciplina
 * (sem vínculo com o livro na modelagem) e o link de cada material, contra a {@link ApiFalsa}.
 */
class DetalhesLivroControllerTests {

	private static final String DUAS_DISCIPLINAS = """
			[{"id": 4, "nome": "Matemática", "codigo": "MAT-1", "descricao": null, "serieEscolar": "PRIMEIRA_SERIE"},
			 {"id": 5, "nome": "Física", "codigo": "FIS-2", "descricao": null, "serieEscolar": "SEGUNDA_SERIE"}]""";

	private static final String TRES_MATERIAIS = """
			[{"id": 10, "titulo": "Lista de exercícios 1", "formato": "PDF",
			  "url": "https://exemplo.com/lista1.pdf", "descricao": null, "dataPublicacao": null,
			  "disciplina": {"id": 4, "nome": "Matemática", "codigo": "MAT-1"}},
			 {"id": 11, "titulo": "Vídeo de revisão", "formato": "VIDEO", "url": null, "descricao": null,
			  "dataPublicacao": null, "disciplina": {"id": 4, "nome": "Matemática", "codigo": "MAT-1"}},
			 {"id": 12, "titulo": "Resumo de Física", "formato": "PDF", "url": "https://exemplo.com/fisica.pdf",
			  "descricao": null, "dataPublicacao": null,
			  "disciplina": {"id": 5, "nome": "Física", "codigo": "FIS-2"}}]""";

	private ApiFalsa api;
	private Parent tela;
	private DetalhesLivroController controlador;

	@BeforeAll
	static void iniciarJavaFx() throws InterruptedException {
		Fx.iniciar();
	}

	@BeforeEach
	void subirApiFalsa() throws IOException {
		api = new ApiFalsa();
		api.responder("GET /api/disciplinas", 200, DUAS_DISCIPLINAS);
		api.responder("GET /api/materiais", 200, TRES_MATERIAIS);
	}

	@AfterEach
	void derrubarApiFalsa() {
		api.close();
	}

	@Test
	void preencherMostraTodosOsDadosDoLivroIncluindoAutorCategoriaEDisciplina() throws Exception {
		LivroResponse livro = new LivroResponse(1L, "Dom Casmurro", "978-85-359-0277-5", 1899,
				"Romance narrado por Bentinho.", "Editora Garnier", new AutorResumo(1L, "Machado de Assis"),
				new CategoriaResumo(2L, "Literatura"), new LivroResponse.DisciplinaResumo(3L, "Língua Portuguesa", "LP-1"));
		abrir();

		naThread(() -> {
			controlador.preencher(livro);
			return null;
		});

		assertThat(naThread(() -> texto("#titulo"))).isEqualTo("Dom Casmurro");
		assertThat(naThread(() -> texto("#autor"))).isEqualTo("Machado de Assis");
		assertThat(naThread(() -> texto("#categoria"))).isEqualTo("Literatura");
		assertThat(naThread(() -> texto("#disciplina"))).isEqualTo("LP-1 — Língua Portuguesa");
		assertThat(naThread(() -> texto("#isbn"))).isEqualTo("978-85-359-0277-5");
		assertThat(naThread(() -> texto("#ano"))).isEqualTo("1899");
		assertThat(naThread(() -> texto("#editora"))).isEqualTo("Editora Garnier");
		assertThat(naThread(() -> texto("#descricao"))).isEqualTo("Romance narrado por Bentinho.");
	}

	@Test
	void preencherUsaTracoQuandoCategoriaDisciplinaAnoEditoraOuDescricaoFaltam() throws Exception {
		LivroResponse semNada = new LivroResponse(2L, "A Hora da Estrela", "9788532508120", null, null, null,
				new AutorResumo(2L, "Clarice Lispector"), null, null);
		abrir();

		naThread(() -> {
			controlador.preencher(semNada);
			return null;
		});

		assertThat(naThread(() -> texto("#categoria"))).isEqualTo("—");
		assertThat(naThread(() -> texto("#disciplina"))).isEqualTo("—");
		assertThat(naThread(() -> texto("#ano"))).isEqualTo("—");
		assertThat(naThread(() -> texto("#editora"))).isEqualTo("—");
		assertThat(naThread(() -> texto("#descricao"))).isEqualTo("—");
	}

	@Test
	void carregarMostraOsMateriaisDeTodasAsDisciplinasPorPadrao() throws Exception {
		abrir();

		naThread(() -> {
			controlador.carregar(new ClienteApi(api.url()));
			return null;
		});

		esperarAte("os materiais chegarem", () -> lista().getItems().size() == 3);
		assertThat(naThread(() -> comboDisciplina().getItems().stream().map(DisciplinaResponse::nome).toList()))
				.containsExactly("Matemática", "Física");
	}

	@Test
	void filtrarPorDisciplinaMostraSoOsMateriaisDaDisciplinaEscolhida() throws Exception {
		abrir();
		naThread(() -> {
			controlador.carregar(new ClienteApi(api.url()));
			return null;
		});
		esperarAte("os materiais chegarem", () -> lista().getItems().size() == 3);

		naThread(() -> {
			comboDisciplina().setValue(comboDisciplina().getItems().get(1));
			return null;
		});

		esperarAte("o filtro de disciplina aplicar", () -> lista().getItems().size() == 1);
		assertThat(naThread(() -> lista().getItems().getFirst().titulo())).isEqualTo("Resumo de Física");
	}

	@Test
	void disciplinasComOMesmoNomeAparecemComCodigoEFiltramPelaEscolhida() throws Exception {
		api.responder("GET /api/disciplinas", 200, """
				[{"id": 4, "nome": "Matemática", "codigo": "MAT-1", "descricao": null, "serieEscolar": "PRIMEIRA_SERIE"},
				 {"id": 5, "nome": "Matemática", "codigo": "MAT-2", "descricao": null, "serieEscolar": "SEGUNDA_SERIE"}]""");
		api.responder("GET /api/materiais", 200, """
				[{"id": 10, "titulo": "Lista da 1ª série", "formato": "PDF", "url": "https://exemplo.com/1.pdf",
				  "descricao": null, "dataPublicacao": null,
				  "disciplina": {"id": 4, "nome": "Matemática", "codigo": "MAT-1"}},
				 {"id": 11, "titulo": "Lista da 2ª série", "formato": "PDF", "url": "https://exemplo.com/2.pdf",
				  "descricao": null, "dataPublicacao": null,
				  "disciplina": {"id": 5, "nome": "Matemática", "codigo": "MAT-2"}}]""");
		abrir();
		naThread(() -> {
			controlador.carregar(new ClienteApi(api.url()));
			return null;
		});
		esperarAte("os materiais chegarem", () -> lista().getItems().size() == 2);

		assertThat(naThread(() -> comboDisciplina().getItems().stream()
				.map(disciplina -> comboDisciplina().getConverter().toString(disciplina)).toList()))
				.containsExactly("MAT-1 — Matemática", "MAT-2 — Matemática");

		naThread(() -> {
			comboDisciplina().setValue(comboDisciplina().getItems().get(1));
			return null;
		});

		esperarAte("o filtro de disciplina aplicar", () -> lista().getItems().size() == 1);
		assertThat(naThread(() -> lista().getItems().getFirst().titulo())).isEqualTo("Lista da 2ª série");
	}

	@Test
	void semMateriaisCadastradosMostraMensagem() throws Exception {
		api.responder("GET /api/materiais", 200, "[]");
		abrir();

		naThread(() -> {
			controlador.carregar(new ClienteApi(api.url()));
			return null;
		});

		esperarAte("a mensagem de nenhum material",
				() -> placeholder().getText().equals("Nenhum material digital cadastrado."));
	}

	@Test
	void erroAoCarregarMostraMensagemDeErro() throws Exception {
		api.responder("GET /api/disciplinas", 500, "");
		abrir();

		naThread(() -> {
			controlador.carregar(new ClienteApi(api.url()));
			return null;
		});

		esperarAte("a mensagem de erro", () -> placeholder().getText().startsWith("Não foi possível carregar"));
		assertThat(naThread(() -> placeholder().getStyleClass())).contains("mensagem-erro");
	}

	@Test
	void linkAusenteMostraAvisoSemChamarONavegador() throws Exception {
		List<URI> abertos = new CopyOnWriteArrayList<>();
		MaterialDigitalResponse semLink = material(null);
		abrir();

		naThread(() -> {
			controlador.abrirNoNavegador = abertos::add;
			controlador.abrirLink(semLink);
			return null;
		});

		assertThat(naThread(() -> texto("#avisoDeLink"))).isEqualTo("Este material não tem um link.");
		assertThat(abertos).isEmpty();
	}

	@Test
	void linkComFormatoInvalidoMostraAvisoSemChamarONavegador() throws Exception {
		List<URI> abertos = new CopyOnWriteArrayList<>();
		MaterialDigitalResponse linkInvalido = material("isto não é uma url");
		abrir();

		naThread(() -> {
			controlador.abrirNoNavegador = abertos::add;
			controlador.abrirLink(linkInvalido);
			return null;
		});

		assertThat(naThread(() -> texto("#avisoDeLink"))).startsWith("Link inválido");
		assertThat(abertos).isEmpty();
	}

	/**
	 * Estes passam na sintaxe de {@code URI}, mas o {@code HostServices} não avisa quando falha: sem esquema
	 * {@code http(s)} e endereço, o clique ficaria sem efeito nenhum em vez de mostrar o aviso.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "www.exemplo.com/lista1.pdf", "/lista1.pdf", "file:///etc/passwd",
			"javascript:alert(1)", "ftp://exemplo.com/lista1.pdf", "https:///lista1.pdf" })
	void linkQueNaoEHttpCompletoMostraAvisoSemChamarONavegador(String url) throws Exception {
		List<URI> abertos = new CopyOnWriteArrayList<>();
		MaterialDigitalResponse naoEhLinkWeb = material(url);
		abrir();

		naThread(() -> {
			controlador.abrirNoNavegador = abertos::add;
			controlador.abrirLink(naoEhLinkWeb);
			return null;
		});

		assertThat(naThread(() -> texto("#avisoDeLink"))).isEqualTo("Link inválido: " + url);
		assertThat(abertos).isEmpty();
	}

	@Test
	void linkHttpEmMaiusculasEComEspacosNasPontasAbre() throws Exception {
		List<URI> abertos = new CopyOnWriteArrayList<>();
		MaterialDigitalResponse comLink = material("  HTTPS://Exemplo.com/lista1.pdf ");
		abrir();

		naThread(() -> {
			controlador.abrirNoNavegador = abertos::add;
			controlador.abrirLink(comLink);
			return null;
		});

		assertThat(abertos).containsExactly(URI.create("HTTPS://Exemplo.com/lista1.pdf"));
		assertThat(naThread(() -> texto("#avisoDeLink"))).isEmpty();
	}

	@Test
	void semNavegadorConfiguradoMostraAvisoEmVezDeNaoFazerNada() throws Exception {
		MaterialDigitalResponse comLink = material("https://exemplo.com/lista1.pdf");
		abrir();

		naThread(() -> {
			controlador.abrirLink(comLink);
			return null;
		});

		assertThat(naThread(() -> texto("#avisoDeLink")))
				.isEqualTo("Não foi possível abrir o link: https://exemplo.com/lista1.pdf");
	}

	@Test
	void linkValidoChamaAbrirNoNavegadorComAUriDoMaterial() throws Exception {
		List<URI> abertos = new CopyOnWriteArrayList<>();
		MaterialDigitalResponse comLink = material("https://exemplo.com/lista1.pdf");
		abrir();

		naThread(() -> {
			controlador.abrirNoNavegador = abertos::add;
			controlador.abrirLink(comLink);
			return null;
		});

		assertThat(abertos).containsExactly(URI.create("https://exemplo.com/lista1.pdf"));
		assertThat(naThread(() -> texto("#avisoDeLink"))).isEmpty();
	}

	@Test
	void falhaAoAbrirMostraAvisoSemDerrubarAAplicacao() throws Exception {
		MaterialDigitalResponse comLink = material("https://exemplo.com/lista1.pdf");
		abrir();

		naThread(() -> {
			controlador.abrirNoNavegador = uri -> {
				throw new IllegalStateException("sem navegador padrão configurado");
			};
			controlador.abrirLink(comLink);
			return null;
		});

		assertThat(naThread(() -> texto("#avisoDeLink")))
				.isEqualTo("Não foi possível abrir o link: https://exemplo.com/lista1.pdf");
	}

	private void abrir() throws Exception {
		naThread(() -> {
			FXMLLoader carregador = new FXMLLoader(DetalhesLivroController.class.getResource("detalhes-livro.fxml"));
			tela = carregador.load();
			controlador = carregador.getController();
			return null;
		});
	}

	private static MaterialDigitalResponse material(String url) {
		return new MaterialDigitalResponse(10L, "Lista de exercícios 1", FormatoMaterial.PDF, url, null, null,
				new DisciplinaResumo(4L, "Matemática", "MAT-1"));
	}

	@SuppressWarnings("unchecked")
	private ListView<MaterialDigitalResponse> lista() {
		return (ListView<MaterialDigitalResponse>) tela.lookup("#listaDeMateriais");
	}

	private Label placeholder() {
		return (Label) lista().getPlaceholder();
	}

	@SuppressWarnings("unchecked")
	private ComboBox<DisciplinaResponse> comboDisciplina() {
		return (ComboBox<DisciplinaResponse>) tela.lookup("#comboDisciplina");
	}

	private String texto(String seletor) {
		return ((Label) tela.lookup(seletor)).getText();
	}

}
