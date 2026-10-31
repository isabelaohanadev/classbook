package br.com.classbook.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.AutorRequest;

/**
 * Confere as rotas de {@code /api/autores} de ponta a ponta, com o contexto Spring completo e o
 * PostgreSQL de teste. A transação é desfeita ao final de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutorControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void cadastroValidoRespondeCreatedComLocationEOAutor() throws Exception {
		AutorRequest request = new AutorRequest("Graciliano Ramos", "Brasileira", null, null);

		mockMvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.nome").value("Graciliano Ramos"));
	}

	@Test
	void nomeAusenteOuEmBrancoRespondeBadRequest() throws Exception {
		AutorRequest request = new AutorRequest("   ", null, null, null);

		mockMvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listagemRespondeOkComAutoresOrdenadosPorNome() throws Exception {
		mockMvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AutorRequest("Machado de Assis", null, null, null))));
		mockMvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AutorRequest("Ariano Suassuna", null, null, null))));

		mockMvc.perform(get("/api/autores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nome").value("Ariano Suassuna"))
				.andExpect(jsonPath("$[1].nome").value("Machado de Assis"));
	}

	@Test
	void listagemSemAutoresRespondeOkComListaVazia() throws Exception {
		mockMvc.perform(get("/api/autores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

}
