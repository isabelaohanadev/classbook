package br.com.classbook.desktop.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.com.classbook.desktop.api.ErroDaApiException.CampoInvalido;
import br.com.classbook.desktop.modelo.AutorRequest;
import br.com.classbook.desktop.modelo.AutorResponse;
import br.com.classbook.desktop.modelo.CategoriaRequest;
import br.com.classbook.desktop.modelo.FormatoMaterial;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.MaterialDigitalRequest;
import br.com.classbook.desktop.modelo.MaterialDigitalResponse;
import br.com.classbook.desktop.modelo.TipoCategoria;

class ClienteApiTests {

	private ApiFalsa api;
	private ClienteApi cliente;

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
	void listarFazGetNaRotaDoRecursoEConverteAsDatas() {
		api.responder(200, """
				[{"id": 1, "nome": "Machado de Assis", "nacionalidade": "Brasileira",
				  "dataNascimento": "1839-06-21", "biografia": null}]""");

		List<AutorResponse> autores = cliente.autores().listar();

		assertThat(api.metodo + " " + api.caminho).isEqualTo("GET /api/autores");
		assertThat(autores).containsExactly(
				new AutorResponse(1L, "Machado de Assis", "Brasileira", LocalDate.of(1839, 6, 21), null));
	}

	@Test
	void cadastrarFazPostComOCorpoEmJsonEDevolveORecursoCriado() {
		api.responder(201, """
				{"id": 7, "titulo": "Lista de funções", "formato": "PDF", "url": "https://exemplo.com/l.pdf",
				 "descricao": null, "dataPublicacao": "2026-03-10",
				 "disciplina": {"id": 2, "nome": "Física", "codigo": "FIS-1"}}""");

		MaterialDigitalResponse criado = cliente.materiais().cadastrar(new MaterialDigitalRequest("Lista de funções",
				FormatoMaterial.PDF, "https://exemplo.com/l.pdf", null, LocalDate.of(2026, 3, 10), 2L));

		assertThat(api.metodo + " " + api.caminho).isEqualTo("POST /api/materiais");
		assertThat(api.tipoDoConteudo).isEqualTo("application/json");
		assertThat(api.corpoRecebido).contains("\"titulo\":\"Lista de funções\"", "\"formato\":\"PDF\"",
				"\"dataPublicacao\":\"2026-03-10\"", "\"disciplinaId\":2");
		assertThat(criado.id()).isEqualTo(7L);
		assertThat(criado.disciplina()).isEqualTo(new MaterialDigitalResponse.DisciplinaResumo(2L, "Física", "FIS-1"));
	}

	@Test
	void buscarEditarEExcluirUsamOIdNaRota() {
		api.responder(200, """
				{"id": 5, "nome": "Literatura", "descricao": null, "faixaEtaria": null, "tipo": "LITERATURA"}""");

		assertThat(cliente.categorias().buscar(5).tipo()).isEqualTo(TipoCategoria.LITERATURA);
		assertThat(api.metodo + " " + api.caminho).isEqualTo("GET /api/categorias/5");

		cliente.categorias().editar(5,
				new CategoriaRequest("Literatura", null, "14 a 17 anos", TipoCategoria.LITERATURA));
		assertThat(api.metodo + " " + api.caminho).isEqualTo("PUT /api/categorias/5");
		assertThat(api.corpoRecebido).contains("\"faixaEtaria\":\"14 a 17 anos\"", "\"tipo\":\"LITERATURA\"");

		api.responder(204, "");
		cliente.categorias().excluir(5);
		assertThat(api.metodo + " " + api.caminho).isEqualTo("DELETE /api/categorias/5");
	}

	@Test
	void cadaUmDosCincoRecursosUsaASuaRota() {
		api.responder(200, "[]");

		cliente.autores().listar();
		assertThat(api.caminho).isEqualTo("/api/autores");
		cliente.livros().listar();
		assertThat(api.caminho).isEqualTo("/api/livros");
		cliente.categorias().listar();
		assertThat(api.caminho).isEqualTo("/api/categorias");
		cliente.disciplinas().listar();
		assertThat(api.caminho).isEqualTo("/api/disciplinas");
		cliente.materiais().listar();
		assertThat(api.caminho).isEqualTo("/api/materiais");
	}

	@Test
	void livroSemCategoriaVemComCategoriaNull() {
		api.responder(200, """
				[{"id": 3, "titulo": "A Hora da Estrela", "isbn": "9788532508120", "anoPublicacao": 1977,
				  "descricao": null, "editora": null, "autor": {"id": 2, "nome": "Clarice Lispector"},
				  "categoria": null}]""");

		LivroResponse livro = cliente.livros().listar().getFirst();

		assertThat(livro.autor()).isEqualTo(new LivroResponse.AutorResumo(2L, "Clarice Lispector"));
		assertThat(livro.categoria()).isNull();
	}

	@Test
	void pesquisarLivrosMandaSoOsFiltrosPreenchidos() {
		api.responder(200, "[]");

		cliente.pesquisarLivros(" Memórias póstumas ", 3L, null);
		assertThat(api.caminho).isEqualTo("/api/livros");
		assertThat(api.consulta).isEqualTo("busca=Mem%C3%B3rias%20p%C3%B3stumas&autorId=3");

		cliente.pesquisarLivros(null, null, 4L);
		assertThat(api.consulta).isEqualTo("categoriaId=4");

		cliente.pesquisarLivros("  ", null, null);
		assertThat(api.consulta).isNull();
	}

	@Test
	void campoNovoNaRespostaNaoQuebraOCliente() {
		api.responder(200, """
				{"id": 1, "nome": "Machado de Assis", "campoQueAInterfaceNaoConhece": true}""");

		assertThat(cliente.autores().buscar(1).nome()).isEqualTo("Machado de Assis");
	}

	@Test
	void erroDeValidacaoViraExcecaoComAMensagemEOsCamposDaApi() {
		api.responder(400, """
				{"horario": "2026-11-02T14:00:00Z", "status": 400, "erro": "Requisição inválida",
				 "mensagem": "Há campos inválidos na requisição.", "caminho": "/api/autores",
				 "campos": [{"campo": "nome", "mensagem": "é obrigatório e não pode ficar em branco"}]}""");

		assertThatThrownBy(() -> cliente.autores().cadastrar(new AutorRequest(" ", null, null, null)))
				.isInstanceOfSatisfying(ErroDaApiException.class, erro -> {
					assertThat(erro.getStatus()).isEqualTo(400);
					assertThat(erro.getMensagem()).isEqualTo("Há campos inválidos na requisição.");
					assertThat(erro.getCampos())
							.containsExactly(new CampoInvalido("nome", "é obrigatório e não pode ficar em branco"));
				})
				.hasMessage("Há campos inválidos na requisição. "
						+ "nome: é obrigatório e não pode ficar em branco.");
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', textBlock = """
			404 | Autor 9 não encontrado.
			409 | Autor 9 possui livros vinculados.
			""")
	void erros404E409TrazemAMensagemRecebida(int status, String mensagem) {
		api.responder(status, """
				{"horario": "2026-11-02T14:00:00Z", "status": %d, "erro": "-", "mensagem": "%s",
				 "caminho": "/api/autores/9", "campos": []}""".formatted(status, mensagem));

		assertThatThrownBy(() -> cliente.autores().excluir(9))
				.isInstanceOfSatisfying(ErroDaApiException.class,
						erro -> assertThat(erro.getStatus()).isEqualTo(status))
				.hasMessage(mensagem);
	}

	@Test
	void corpoDeErroForaDoFormatoDaApiViraMensagemGenerica() {
		api.responder(502, "<html>Bad Gateway</html>");

		assertThatThrownBy(() -> cliente.livros().listar())
				.isInstanceOf(ErroDaApiException.class)
				.hasMessage("A API respondeu com o código 502.");
	}

	@Test
	void apiForaDoArViraApiIndisponivelComAUrlTentada() throws IOException {
		int portaSemServidor;
		try (ServerSocket socket = new ServerSocket(0)) {
			portaSemServidor = socket.getLocalPort();
		}
		String url = "http://127.0.0.1:" + portaSemServidor + "/api";

		assertThatThrownBy(() -> new ClienteApi(url).autores().listar())
				.isInstanceOf(ApiIndisponivelException.class)
				.hasMessage("Não foi possível falar com a API em " + url + ". Confira se o backend está rodando.");
	}

	@Test
	void urlBaseVemDaPropriedadeDeSistemaOuDoPadrao() {
		assumeTrue(System.getenv("CLASSBOOK_API_URL") == null, "CLASSBOOK_API_URL definida no ambiente");
		String anterior = System.getProperty("classbook.api.url");
		try {
			System.setProperty("classbook.api.url", " http://servidor:9090/api/ ");
			assertThat(new ClienteApi().getUrlBase()).isEqualTo("http://servidor:9090/api");

			System.clearProperty("classbook.api.url");
			assertThat(ClienteApi.urlConfigurada()).isEqualTo("http://localhost:8080/api");
		} finally {
			if (anterior == null) {
				System.clearProperty("classbook.api.url");
			} else {
				System.setProperty("classbook.api.url", anterior);
			}
		}
	}

}
