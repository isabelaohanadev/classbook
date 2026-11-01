package br.com.classbook.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.SerieEscolar;

/**
 * Confere as rotas de {@code /api/materiais} de ponta a ponta, com o contexto Spring completo e o
 * PostgreSQL de teste. A transação é desfeita ao final de cada teste, e nenhum teste supõe que o banco
 * começa vazio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaterialDigitalControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private Long disciplinaId;

	@BeforeEach
	void cadastrarDisciplina() throws Exception {
		disciplinaId = cadastrar("/api/disciplinas",
				new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));
	}

	@Test
	void cadastroValidoRespondeCreatedComLocationEADisciplinaResumida() throws Exception {
		MaterialDigitalRequest request = new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF,
				"https://exemplo.com/funcoes.pdf", "Texto opcional", LocalDate.of(2026, 3, 10), disciplinaId);

		MvcResult resultado = enviar(post("/api/materiais"), request)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.titulo").value("Lista de funções"))
				.andExpect(jsonPath("$.formato").value("PDF"))
				.andExpect(jsonPath("$.url").value("https://exemplo.com/funcoes.pdf"))
				.andExpect(jsonPath("$.dataPublicacao").value("2026-03-10"))
				.andExpect(jsonPath("$.disciplina.id").value(disciplinaId))
				.andExpect(jsonPath("$.disciplina.nome").value("Matemática"))
				.andExpect(jsonPath("$.disciplina.codigo").value("MAT-1"))
				.andReturn();

		Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
		assertThat(resultado.getResponse().getHeader("Location")).endsWith("/api/materiais/" + id.longValue());
	}

	@Test
	void cadastroSemDisciplinaIdRespondeBadRequest() throws Exception {
		enviar(post("/api/materiais"), material("Lista de funções", "https://exemplo.com/a.pdf", null))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComUrlInvalidaRespondeBadRequest() throws Exception {
		enviar(post("/api/materiais"), material("Lista de funções", "ftp://exemplo.com/a.pdf", disciplinaId))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComFormatoForaDaListaRespondeBadRequest() throws Exception {
		String corpo = """
				{"titulo": "Lista", "formato": "pdf", "url": "https://exemplo.com/a.pdf", "disciplinaId": %d}
				""".formatted(disciplinaId);

		mockMvc.perform(post("/api/materiais").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listagemRespondeOkComMateriaisOrdenadosPorTitulo() throws Exception {
		cadastrar("/api/materiais", material("Videoaula de funções", "https://exemplo.com/v", disciplinaId));
		cadastrar("/api/materiais", material("Apostila de funções", "https://exemplo.com/a.pdf", disciplinaId));
		cadastrar("/api/materiais", material("Lista de funções", "https://exemplo.com/l.pdf", disciplinaId));

		String corpo = mockMvc.perform(get("/api/materiais"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		List<String> titulos = JsonPath.read(corpo, "$[*].titulo");
		assertThat(titulos)
				.containsSubsequence("Apostila de funções", "Lista de funções", "Videoaula de funções");
	}

	@Test
	void consultaPorIdExistenteRespondeOkComOMaterial() throws Exception {
		Long id = cadastrar("/api/materiais",
				material("Lista de funções", "https://exemplo.com/l.pdf", disciplinaId));

		mockMvc.perform(get("/api/materiais/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.titulo").value("Lista de funções"))
				.andExpect(jsonPath("$.disciplina.codigo").value("MAT-1"));
	}

	@Test
	void consultaPorIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(get("/api/materiais/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void edicaoValidaRespondeOkComOMaterialAtualizado() throws Exception {
		Long id = cadastrar("/api/materiais", new MaterialDigitalRequest("Título antigo", FormatoMaterial.PDF,
				"https://exemplo.com/antigo.pdf", "Texto", LocalDate.of(2026, 3, 10), disciplinaId));

		enviar(put("/api/materiais/{id}", id), new MaterialDigitalRequest("Título novo", FormatoMaterial.VIDEO,
				"https://exemplo.com/novo", null, null, disciplinaId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Título novo"))
				.andExpect(jsonPath("$.formato").value("VIDEO"))
				.andExpect(jsonPath("$.descricao").value(nullValue()))
				.andExpect(jsonPath("$.dataPublicacao").value(nullValue()));
	}

	@Test
	void edicaoComIdInexistenteRespondeNotFound() throws Exception {
		enviar(put("/api/materiais/{id}", -1), material("Título", "https://exemplo.com/a.pdf", disciplinaId))
				.andExpect(status().isNotFound());
	}

	@Test
	void edicaoComDadosInvalidosRespondeBadRequest() throws Exception {
		Long id = cadastrar("/api/materiais", material("Título", "https://exemplo.com/a.pdf", disciplinaId));

		enviar(put("/api/materiais/{id}", id), material("Título", "exemplo.com/a.pdf", disciplinaId))
				.andExpect(status().isBadRequest());
	}

	@Test
	void exclusaoDeMaterialExistenteRespondeNoContent() throws Exception {
		Long id = cadastrar("/api/materiais", material("Descartável", "https://exemplo.com/a.pdf", disciplinaId));

		mockMvc.perform(delete("/api/materiais/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/materiais/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoComIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(delete("/api/materiais/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void cadastroComDisciplinaInexistenteRespondeBadRequestComMensagemClara() throws Exception {
		MvcResult resultado = enviar(post("/api/materiais"), material("Lista", "https://exemplo.com/a.pdf", -1L))
				.andExpect(status().isBadRequest())
				.andReturn();

		assertThat(resultado.getResolvedException()).hasMessage("Disciplina -1 não existe.");
	}

	@Test
	void resumoDaDisciplinaTrazSoIdNomeECodigo() throws Exception {
		Long id = cadastrar("/api/materiais", material("Lista", "https://exemplo.com/a.pdf", disciplinaId));

		String corpo = mockMvc.perform(get("/api/materiais/{id}", id))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Map<String, Object> disciplina = JsonPath.read(corpo, "$.disciplina");
		assertThat(disciplina).containsOnlyKeys("id", "nome", "codigo");
	}

	@Test
	void exclusaoDeMaterialMantemADisciplina() throws Exception {
		Long id = cadastrar("/api/materiais", material("Lista", "https://exemplo.com/a.pdf", disciplinaId));

		mockMvc.perform(delete("/api/materiais/{id}", id)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/disciplinas/{id}", disciplinaId)).andExpect(status().isOk());
	}

	private static MaterialDigitalRequest material(String titulo, String url, Long disciplinaId) {
		return new MaterialDigitalRequest(titulo, FormatoMaterial.PDF, url, null, null, disciplinaId);
	}

	private ResultActions enviar(MockHttpServletRequestBuilder requisicao, Object corpo) throws Exception {
		return mockMvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(corpo)));
	}

	private Long cadastrar(String rota, Object corpo) throws Exception {
		String resposta = enviar(post(rota), corpo)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(resposta, "$.id")).longValue();
	}

}
