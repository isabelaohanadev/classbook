package br.com.classbook.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.ThreadLocalRandom;

import com.jayway.jsonpath.JsonPath;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.exception.ErroResponse.CampoInvalido;
import br.com.classbook.model.TipoCategoria;

/**
 * Confere que cada tipo de erro sai do {@link TratadorGlobalDeExcecoes} com o status certo e no formato
 * de {@link ErroResponse}, com mensagem em português e sem detalhes internos. Usa as rotas de autores e
 * livros, que já estão na {@code main}; a transação é desfeita ao final de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TratadorGlobalDeExcecoesTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void recursoInexistenteResponde404NoFormatoPadrao() throws Exception {
		mockMvc.perform(get("/api/autores/{id}", -1))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.horario").isNotEmpty())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Não encontrado"))
				.andExpect(jsonPath("$.mensagem").value("Autor -1 não encontrado."))
				.andExpect(jsonPath("$.caminho").value("/api/autores/-1"))
				.andExpect(jsonPath("$.campos").isEmpty());
	}

	@Test
	void campoInvalidoResponde400ListandoOsCamposEmPortugues() throws Exception {
		enviarJson(post("/api/autores"), """
				{"nome": "   ", "dataNascimento": "2999-01-01"}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Requisição inválida"))
				.andExpect(jsonPath("$.mensagem").value("Há campos inválidos na requisição."))
				.andExpect(jsonPath("$.campos.length()").value(2))
				.andExpect(jsonPath("$.campos[0].campo").value("dataNascimento"))
				.andExpect(jsonPath("$.campos[0].mensagem").value("deve ser uma data no passado"))
				.andExpect(jsonPath("$.campos[1].campo").value("nome"))
				.andExpect(jsonPath("$.campos[1].mensagem").value("é obrigatório e não pode ficar em branco"));
	}

	@Test
	void regraDoServiceResponde400ComAMensagemDaExcecao() throws Exception {
		enviarJson(post("/api/livros"), """
				{"titulo": "Dom Casmurro", "isbn": "%s", "autorId": -1}
				""".formatted(isbnAleatorio()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Autor -1 não existe."))
				.andExpect(jsonPath("$.campos").isEmpty());
	}

	@Test
	void conflitoDeDominioResponde409ComAMensagemDaExcecao() throws Exception {
		Number autorId = cadastrarAutor();
		cadastrarLivro(autorId, isbnAleatorio());

		mockMvc.perform(delete("/api/autores/{id}", autorId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("Conflito"))
				.andExpect(jsonPath("$.mensagem").value("Autor " + autorId + " possui livros vinculados."));
	}

	@Test
	void violacaoDeIntegridadeResponde409SemExporOBanco() throws Exception {
		Number autorId = cadastrarAutor();
		String isbn = isbnAleatorio();
		cadastrarLivro(autorId, isbn);

		// O service ainda não confere ISBN repetido (Issue #46): é o índice único do banco que barra.
		enviarJson(post("/api/livros"), """
				{"titulo": "Outro título", "isbn": "%s", "autorId": %d}
				""".formatted(isbn, autorId.longValue()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensagem").value(
						"A operação conflita com um registro existente: dado duplicado ou registro ainda em uso."))
				.andExpect(content().string(not(containsString("uk_livro_isbn"))))
				.andExpect(content().string(not(containsString("insert"))));
	}

	@Test
	void jsonMalformadoResponde400() throws Exception {
		enviarJson(post("/api/autores"), "{\"nome\": ")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("O corpo da requisição não é um JSON válido."))
				.andExpect(jsonPath("$.campos").isEmpty());
	}

	@Test
	void valorDeTipoErradoNoCorpoResponde400ApontandoOCampo() throws Exception {
		enviarJson(post("/api/livros"), """
				{"titulo": "Dom Casmurro", "isbn": "%s", "autorId": "abc"}
				""".formatted(isbnAleatorio()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("autorId"))
				.andExpect(jsonPath("$.campos[0].mensagem").value("valor com tipo ou formato inválido"));
	}

	@Test
	void idDaRotaQueNaoEhNumeroResponde400() throws Exception {
		mockMvc.perform(get("/api/autores/{id}", "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("O valor 'abc' não é válido para 'id'."));
	}

	@Test
	void rotaInexistenteResponde404NoFormatoPadrao() throws Exception {
		mockMvc.perform(get("/api/inexistente"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Rota não encontrada."))
				.andExpect(jsonPath("$.caminho").value("/api/inexistente"));
	}

	@Test
	void metodoNaoSuportadoResponde405NoFormatoPadrao() throws Exception {
		mockMvc.perform(patch("/api/autores"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.erro").value("Método não permitido"))
				.andExpect(jsonPath("$.mensagem").value("Método HTTP não suportado nesta rota."));
	}

	@Test
	void corpoQueNaoEhJsonResponde415NoFormatoPadrao() throws Exception {
		mockMvc.perform(post("/api/autores").contentType(MediaType.TEXT_PLAIN).content("Machado de Assis"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.status").value(415));
	}

	@Test
	void enumForaDaListaApontaOCampoEOsValoresAceitos() {
		JacksonException causa = InvalidFormatException.from(null, "valor inválido", "literatura",
				TipoCategoria.class);
		causa.prependPath(new JacksonException.Reference(Object.class, "tipo"));
		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("corpo inválido", causa,
				new MockHttpInputMessage(new byte[0]));

		ResponseEntity<ErroResponse> resposta = new TratadorGlobalDeExcecoes().corpoIlegivel(ex,
				new MockHttpServletRequest("POST", "/api/categorias"));

		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(resposta.getBody().campos()).containsExactly(new CampoInvalido("tipo",
				"valor 'literatura' fora da lista; use um de: DIDATICO, PARADIDATICO, LITERATURA, REFERENCIA, OUTRO"));
	}

	@Test
	void erroInesperadoResponde500SemExporDetalhes() {
		ResponseEntity<ErroResponse> resposta = new TratadorGlobalDeExcecoes().inesperado(
				new IllegalStateException("senha=segredo em jdbc:postgresql://banco"),
				new MockHttpServletRequest("GET", "/api/autores"));

		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(resposta.getBody().erro()).isEqualTo("Erro interno");
		assertThat(resposta.getBody().mensagem()).isEqualTo("Erro inesperado no servidor. Tente novamente.")
				.doesNotContain("senha");
	}

	private ResultActions enviarJson(MockHttpServletRequestBuilder requisicao, String corpo) throws Exception {
		return mockMvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	private Number cadastrarAutor() throws Exception {
		String corpo = enviarJson(post("/api/autores"), "{\"nome\": \"Machado de Assis\"}")
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(corpo, "$.id");
	}

	private void cadastrarLivro(Number autorId, String isbn) throws Exception {
		enviarJson(post("/api/livros"), """
				{"titulo": "Dom Casmurro", "isbn": "%s", "autorId": %d}
				""".formatted(isbn, autorId.longValue()))
				.andExpect(status().isCreated());
	}

	/** ISBN-13 aleatório, para não depender dos livros que já existam no banco de teste. */
	private static String isbnAleatorio() {
		return "978" + String.format("%010d", ThreadLocalRandom.current().nextLong(10_000_000_000L));
	}

}
