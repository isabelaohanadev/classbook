package br.com.classbook.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Year;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.AutorRequest;
import br.com.classbook.dto.LivroRequest;

/**
 * Confere as rotas de {@code /api/livros} de ponta a ponta, com o contexto Spring completo e o
 * PostgreSQL de teste. A transação é desfeita ao final de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LivroControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void cadastroValidoRespondeCreatedComLocationEOAutorResumido() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
				.andExpect(jsonPath("$.autor.id").value(autorId))
				.andExpect(jsonPath("$.autor.nome").value("Machado de Assis"));
	}

	@Test
	void cadastroComAutorIdAusenteRespondeBadRequest() throws Exception {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComAnoDePublicacaoNoFuturoRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest request = new LivroRequest("Livro do Futuro", "9780306406157", Year.now().getValue() + 1, null,
				null, autorId);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComAutorInexistenteRespondeBadRequest() throws Exception {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, -1L);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listagemRespondeOkComLivrosOrdenadosPorTitulo() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId));
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId));

		mockMvc.perform(get("/api/livros"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"))
				.andExpect(jsonPath("$[1].titulo").value("Memórias Póstumas"));
	}

	@Test
	void pesquisaPorTituloRetornaCorrespondentesIgnorandoCaixa() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId));
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId));

		mockMvc.perform(get("/api/livros").param("titulo", "casmurro"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
	}

	@Test
	void pesquisaSemCorrespondenciaRespondeOkComListaVazia() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId));

		mockMvc.perform(get("/api/livros").param("titulo", "inexistente"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void listagemSemLivrosRespondeOkComListaVazia() throws Exception {
		mockMvc.perform(get("/api/livros"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void consultaPorIdExistenteRespondeOkComOLivro() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId));

		mockMvc.perform(get("/api/livros/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Dom Casmurro"));
	}

	@Test
	void consultaPorIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(get("/api/livros/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void edicaoValidaRespondeOkComOLivroAtualizado() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título Antigo", "9780306406157", null, null, null, autorId));
		LivroRequest edicao = new LivroRequest("Título Novo", "9780316000000", 2020, null, null, autorId);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Título Novo"))
				.andExpect(jsonPath("$.isbn").value("9780316000000"));
	}

	@Test
	void edicaoComIdInexistenteRespondeNotFound() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId);

		mockMvc.perform(put("/api/livros/{id}", -1).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isNotFound());
	}

	@Test
	void edicaoComDadosInvalidosRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId));
		LivroRequest edicao = new LivroRequest("   ", "9780306406157", null, null, null, autorId);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void exclusaoDeLivroExistenteRespondeNoContent() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId));

		mockMvc.perform(delete("/api/livros/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/livros/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoComIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(delete("/api/livros/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoDeLivroMantemOAutor() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long livroId = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId));

		mockMvc.perform(delete("/api/livros/{id}", livroId)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/autores/{id}", autorId)).andExpect(status().isOk());
	}

	private Long cadastrarAutor(String nome) throws Exception {
		MvcResult resultado = mockMvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AutorRequest(nome, null, null, null))))
				.andReturn();
		return corpoComoJson(resultado).get("id").asLong();
	}

	private Long cadastrarLivro(LivroRequest request) throws Exception {
		MvcResult resultado = mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andReturn();
		return corpoComoJson(resultado).get("id").asLong();
	}

	private JsonNode corpoComoJson(MvcResult resultado) throws Exception {
		return objectMapper.readTree(resultado.getResponse().getContentAsString());
	}

}
