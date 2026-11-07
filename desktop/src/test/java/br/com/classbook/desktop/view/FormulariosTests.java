package br.com.classbook.desktop.view;

import static br.com.classbook.desktop.view.Fx.esperarAte;
import static br.com.classbook.desktop.view.Fx.naThread;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.classbook.desktop.api.ApiFalsa;
import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.AutorResponse;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.FormatoMaterial;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.SerieEscolar;

/**
 * Os critérios da #58 e da #61 no formulário de verdade, com cliques em Salvar e Cancelar: os campos viram
 * o JSON certo, os obrigatórios vazios e o Cancelar não chamam a API, as listas vêm da API e o erro da
 * API aparece no formulário, que continua aberto.
 */
class FormulariosTests {

	private ApiFalsa api;
	private ClienteApi cliente;
	private final List<Object> salvos = new CopyOnWriteArrayList<>();

	@BeforeAll
	static void iniciarJavaFx() throws InterruptedException {
		Fx.iniciar();
	}

	@BeforeEach
	void subirApiFalsa() throws IOException {
		api = new ApiFalsa();
		cliente = new ClienteApi(api.url());
	}

	@AfterEach
	void derrubarApiFalsa() {
		api.close();
	}

	@Test
	void cadastroDeAutorEnviaPostComOsCamposEFechaOFormulario() throws Exception {
		api.responder("POST /api/autores", 201, """
				{"id": 7, "nome": "Machado de Assis", "nacionalidade": null, "dataNascimento": "1839-06-21"}""");
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.autores(), null);

		naThread(() -> {
			campo(janela, "nome").setText("  Machado de Assis ");
			campo(janela, "dataNascimento").setText("21/06/1839");
			clicar(janela, JanelaDeFormulario.SALVAR);
			return null;
		});
		esperarAte("a API confirmar o cadastro", () -> !salvos.isEmpty());

		assertThat(api.alteracoes()).hasSize(1).first().satisfies(requisicao -> {
			assertThat(requisicao.rota()).isEqualTo("POST /api/autores");
			assertThat(requisicao.corpo()).contains("\"nome\":\"Machado de Assis\"",
					"\"dataNascimento\":\"1839-06-21\"", "\"nacionalidade\":null");
		});
		assertThat(((AutorResponse) salvos.getFirst()).id()).isEqualTo(7L);
		assertThat(naThread(() -> janela.dialogo().isShowing())).isFalse();
	}

	@Test
	void obrigatoriosVaziosSaoMarcadosENaoChamamAApi() throws Exception {
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.categorias(), null);

		naThread(() -> {
			campo(janela, "nome").setText("   ");
			clicar(janela, JanelaDeFormulario.SALVAR);
			return null;
		});

		assertThat(naThread(janela::mensagemDeErro)).isEqualTo("Preencha os campos obrigatórios: Nome, Tipo.");
		assertThat(naThread(() -> campo(janela, "nome").getStyleClass())).contains("campo-obrigatorio-vazio");
		assertThat(naThread(() -> janela.dialogo().isShowing())).isTrue();
		assertThat(api.recebidas()).isEmpty();
	}

	@Test
	void cancelarDescartaAsAlteracoesSemChamarAApi() throws Exception {
		AutorResponse autor = new AutorResponse(3L, "Clarice Lispector", "Brasileira", null, null);
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.autores(), autor);

		assertThat(naThread(() -> campo(janela, "nome").getText())).isEqualTo("Clarice Lispector");
		naThread(() -> {
			campo(janela, "nome").setText("Outro nome");
			clicar(janela, ButtonType.CANCEL);
			return null;
		});

		assertThat(naThread(() -> janela.dialogo().isShowing())).isFalse();
		assertThat(api.recebidas()).isEmpty();
		assertThat(salvos).isEmpty();
	}

	@Test
	void dataInvalidaMostraAMensagemSemChamarAApi() throws Exception {
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.autores(), null);

		naThread(() -> {
			campo(janela, "nome").setText("Machado de Assis");
			campo(janela, "dataNascimento").setText("31/02/1839");
			clicar(janela, JanelaDeFormulario.SALVAR);
			return null;
		});

		assertThat(naThread(janela::mensagemDeErro))
				.isEqualTo("A data de nascimento deve ser uma data válida no formato dd/mm/aaaa.");
		assertThat(api.recebidas()).isEmpty();
	}

	@Test
	void erroDaApiApareceNoFormularioQueContinuaAbertoComOQueFoiDigitado() throws Exception {
		api.responder("POST /api/disciplinas", 409, """
				{"horario": "2026-11-04T12:00:00Z", "status": 409, "erro": "Conflito",
				 "mensagem": "Já existe uma disciplina com o código MAT-1.", "caminho": "/api/disciplinas",
				 "campos": []}""");
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.disciplinas(), null);

		naThread(() -> {
			campo(janela, "nome").setText("Matemática");
			campo(janela, "codigo").setText("mat-1");
			this.<SerieEscolar>escolha(janela, "serieEscolar").setValue(SerieEscolar.PRIMEIRA_SERIE);
			clicar(janela, JanelaDeFormulario.SALVAR);
			return null;
		});
		esperarAte("a mensagem da API aparecer", () -> !janela.mensagemDeErro().isEmpty());

		assertThat(naThread(janela::mensagemDeErro)).isEqualTo("Já existe uma disciplina com o código MAT-1.");
		assertThat(naThread(() -> janela.dialogo().isShowing())).isTrue();
		assertThat(naThread(() -> campo(janela, "codigo").getText())).isEqualTo("mat-1");
		assertThat(naThread(() -> janela.dialogo().getDialogPane().lookupButton(JanelaDeFormulario.SALVAR)
				.isDisabled())).isFalse();
		assertThat(salvos).isEmpty();
	}

	@Test
	void edicaoDeLivroCarregaAutorCategoriaEDisciplinaDaApiEEnviaPut() throws Exception {
		api.responder("GET /api/autores", 200, """
				[{"id": 1, "nome": "Clarice Lispector"}, {"id": 2, "nome": "Machado de Assis"}]""");
		api.responder("GET /api/categorias", 200, """
				[{"id": 5, "nome": "Literatura", "tipo": "LITERATURA"}]""");
		api.responder("GET /api/disciplinas", 200, """
				[{"id": 6, "nome": "Matemática", "codigo": "MAT-1", "serieEscolar": "PRIMEIRA_SERIE"},
				 {"id": 7, "nome": "Língua Portuguesa", "codigo": "LP-1", "serieEscolar": "PRIMEIRA_SERIE"}]""");
		api.responder("PUT /api/livros/3", 200, """
				{"id": 3, "titulo": "Dom Casmurro (edição revista)", "isbn": "9788535902778",
				 "autor": {"id": 2, "nome": "Machado de Assis"}, "categoria": {"id": 5, "nome": "Literatura"},
				 "disciplina": {"id": 7, "nome": "Língua Portuguesa", "codigo": "LP-1"}}""");
		LivroResponse livro = new LivroResponse(3L, "Dom Casmurro", "9788535902778", null, null, null,
				new LivroResponse.AutorResumo(2L, "Machado de Assis"),
				new LivroResponse.CategoriaResumo(5L, "Literatura"),
				new LivroResponse.DisciplinaResumo(7L, "Língua Portuguesa", "LP-1"));
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.livros(), livro);

		esperarAte("as listas chegarem", () -> escolha(janela, "autor").getValue() != null);
		assertThat(naThread(() -> escolha(janela, "autor").getItems())).hasSize(2);
		assertThat(naThread(() -> this.<DisciplinaResponse>escolha(janela, "disciplina").getItems())).hasSize(2);
		assertThat(naThread(() -> this.<DisciplinaResponse>escolha(janela, "disciplina").getValue().id()))
				.isEqualTo(7L);
		naThread(() -> {
			campo(janela, "titulo").setText("Dom Casmurro (edição revista)");
			clicar(janela, JanelaDeFormulario.SALVAR);
			return null;
		});
		esperarAte("a API confirmar a edição", () -> !salvos.isEmpty());

		assertThat(api.alteracoes()).hasSize(1).first().satisfies(requisicao -> {
			assertThat(requisicao.rota()).isEqualTo("PUT /api/livros/3");
			assertThat(requisicao.corpo()).contains("\"titulo\":\"Dom Casmurro (edição revista)\"", "\"autorId\":2",
					"\"categoriaId\":5", "\"disciplinaId\":7", "\"anoPublicacao\":null");
		});
	}

	@Test
	void edicaoDeLivroSemDisciplinaEnviaDisciplinaIdNulo() throws Exception {
		api.responder("GET /api/autores", 200, """
				[{"id": 2, "nome": "Machado de Assis"}]""");
		api.responder("GET /api/categorias", 200, "[]");
		api.responder("GET /api/disciplinas", 200, """
				[{"id": 7, "nome": "Língua Portuguesa", "codigo": "LP-1", "serieEscolar": "PRIMEIRA_SERIE"}]""");
		api.responder("PUT /api/livros/3", 200, """
				{"id": 3, "titulo": "Dom Casmurro", "isbn": "9788535902778",
				 "autor": {"id": 2, "nome": "Machado de Assis"}}""");
		LivroResponse livro = new LivroResponse(3L, "Dom Casmurro", "9788535902778", null, null, null,
				new LivroResponse.AutorResumo(2L, "Machado de Assis"), null, null);
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.livros(), livro);

		esperarAte("as listas chegarem", () -> escolha(janela, "autor").getValue() != null);
		assertThat(naThread(() -> this.<DisciplinaResponse>escolha(janela, "disciplina").getValue())).isNull();
		naThread(() -> {
			clicar(janela, JanelaDeFormulario.SALVAR);
			return null;
		});
		esperarAte("a API confirmar a edição", () -> !salvos.isEmpty());

		assertThat(api.alteracoes()).hasSize(1).first().satisfies(requisicao -> {
			assertThat(requisicao.rota()).isEqualTo("PUT /api/livros/3");
			assertThat(requisicao.corpo()).contains("\"disciplinaId\":null", "\"categoriaId\":null");
		});
	}

	@Test
	void cadastroDeMaterialUsaADisciplinaEscolhidaNaListaDaApi() throws Exception {
		api.responder("GET /api/disciplinas", 200, """
				[{"id": 4, "nome": "Física", "codigo": "FIS-1", "serieEscolar": "PRIMEIRA_SERIE"}]""");
		api.responder("POST /api/materiais", 201, """
				{"id": 9, "titulo": "Lista", "formato": "PDF", "url": "https://exemplo.com/l.pdf",
				 "disciplina": {"id": 4, "nome": "Física", "codigo": "FIS-1"}}""");
		JanelaDeFormulario<?, ?> janela = abrir(Cadastro.materiais(), null);

		esperarAte("a lista de disciplinas chegar", () -> !escolha(janela, "disciplina").getItems().isEmpty());
		naThread(() -> {
			campo(janela, "titulo").setText("Lista");
			this.<FormatoMaterial>escolha(janela, "formato").setValue(FormatoMaterial.PDF);
			campo(janela, "url").setText("https://exemplo.com/l.pdf");
			this.<DisciplinaResponse>escolha(janela, "disciplina")
					.setValue(this.<DisciplinaResponse>escolha(janela, "disciplina").getItems().getFirst());
			campo(janela, "dataPublicacao").setText("10/03/2026");
			clicar(janela, JanelaDeFormulario.SALVAR);
			return null;
		});
		esperarAte("a API confirmar o cadastro", () -> !salvos.isEmpty());

		assertThat(api.alteracoes()).singleElement().satisfies(requisicao -> {
			assertThat(requisicao.rota()).isEqualTo("POST /api/materiais");
			assertThat(requisicao.corpo()).contains("\"formato\":\"PDF\"", "\"disciplinaId\":4",
					"\"dataPublicacao\":\"" + LocalDate.of(2026, 3, 10) + "\"");
		});
	}

	private <Q, R> JanelaDeFormulario<Q, R> abrir(Cadastro<Q, R> cadastro, R registro) throws Exception {
		return naThread(() -> {
			JanelaDeFormulario<Q, R> janela = new JanelaDeFormulario<>(cadastro, cliente, registro, salvos::add);
			janela.abrir(null);
			return janela;
		});
	}

	private static TextField campo(JanelaDeFormulario<?, ?> janela, String id) {
		return (TextField) janela.dialogo().getDialogPane().lookup("#" + id);
	}

	@SuppressWarnings("unchecked")
	private <T> ComboBox<T> escolha(JanelaDeFormulario<?, ?> janela, String id) {
		return (ComboBox<T>) janela.dialogo().getDialogPane().lookup("#" + id);
	}

	private static void clicar(JanelaDeFormulario<?, ?> janela, ButtonType botao) {
		((Button) janela.dialogo().getDialogPane().lookupButton(botao)).fire();
	}

}
