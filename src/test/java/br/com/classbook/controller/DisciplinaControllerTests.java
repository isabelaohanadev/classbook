package br.com.classbook.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.jayway.jsonpath.JsonPath;
import tools.jackson.databind.ObjectMapper;

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
import br.com.classbook.model.SerieEscolar;

/**
 * Confere as rotas de {@code /api/disciplinas} de ponta a ponta, com o contexto Spring completo e o
 * PostgreSQL de teste. A transação é desfeita ao final de cada teste, e nenhum teste supõe que o banco
 * começa vazio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DisciplinaControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void cadastroValidoRespondeCreatedComLocationEOCodigoNormalizado() throws Exception {
		DisciplinaRequest request = new DisciplinaRequest("Matemática", " mat-1 ", "Matemática da 1ª série",
				SerieEscolar.PRIMEIRA_SERIE);

		MvcResult resultado = enviar(post("/api/disciplinas"), request)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Matemática"))
				.andExpect(jsonPath("$.codigo").value("MAT-1"))
				.andExpect(jsonPath("$.descricao").value("Matemática da 1ª série"))
				.andExpect(jsonPath("$.serieEscolar").value("PRIMEIRA_SERIE"))
				.andReturn();

		Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
		assertThat(resultado.getResponse().getHeader("Location")).endsWith("/api/disciplinas/" + id.longValue());
	}

	@Test
	void cadastroSemCamposObrigatoriosRespondeBadRequest() throws Exception {
		enviar(post("/api/disciplinas"), new DisciplinaRequest(null, "   ", null, null))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComSerieEscolarForaDaListaRespondeBadRequest() throws Exception {
		String corpo = """
				{"nome": "Matemática", "codigo": "MAT-1", "serieEscolar": "primeira_serie"}
				""";

		mockMvc.perform(post("/api/disciplinas").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComCodigoJaUsadoRespondeConflict() throws Exception {
		cadastrar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(post("/api/disciplinas"), new DisciplinaRequest("Outra", "mat-1", null, SerieEscolar.SEGUNDA_SERIE))
				.andExpect(status().isConflict());
	}

	@Test
	void listagemRespondeOkComDisciplinasOrdenadasPorCodigo() throws Exception {
		cadastrar(new DisciplinaRequest("Química", "QUI-2", null, SerieEscolar.SEGUNDA_SERIE));
		cadastrar(new DisciplinaRequest("Física", "FIS-1", null, SerieEscolar.PRIMEIRA_SERIE));
		cadastrar(new DisciplinaRequest("Matemática", "MAT-3", null, SerieEscolar.TERCEIRA_SERIE));

		String corpo = mockMvc.perform(get("/api/disciplinas"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		List<String> codigos = JsonPath.read(corpo, "$[*].codigo");
		assertThat(codigos).containsSubsequence("FIS-1", "MAT-3", "QUI-2");
	}

	@Test
	void consultaPorIdExistenteRespondeOkComADisciplina() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		mockMvc.perform(get("/api/disciplinas/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.codigo").value("MAT-1"));
	}

	@Test
	void consultaPorIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(get("/api/disciplinas/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void edicaoValidaRespondeOkComADisciplinaAtualizada() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Nome Antigo", "ANT-1", "Descrição antiga",
				SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Nome Novo", "nov-2", null,
				SerieEscolar.SEGUNDA_SERIE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Nome Novo"))
				.andExpect(jsonPath("$.codigo").value("NOV-2"))
				.andExpect(jsonPath("$.descricao").value(nullValue()))
				.andExpect(jsonPath("$.serieEscolar").value("SEGUNDA_SERIE"));
	}

	@Test
	void edicaoComIdInexistenteRespondeNotFound() throws Exception {
		enviar(put("/api/disciplinas/{id}", -1), new DisciplinaRequest("Nome", "COD-1", null,
				SerieEscolar.PRIMEIRA_SERIE))
				.andExpect(status().isNotFound());
	}

	@Test
	void edicaoComDadosInvalidosRespondeBadRequest() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Matemática", "MAT-1", null, null))
				.andExpect(status().isBadRequest());
	}

	@Test
	void edicaoComCodigoDeOutraDisciplinaRespondeConflict() throws Exception {
		cadastrar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));
		Long id = cadastrar(new DisciplinaRequest("Física", "FIS-1", null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Física", "MAT-1", null,
				SerieEscolar.PRIMEIRA_SERIE))
				.andExpect(status().isConflict());
	}

	@Test
	void edicaoMantendoOProprioCodigoRespondeOk() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Matemática Básica", "mat-1", null,
				SerieEscolar.PRIMEIRA_SERIE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Matemática Básica"));
	}

	@Test
	void exclusaoDeDisciplinaSemMateriaisRespondeNoContent() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Descartável", "DESC-1", null, SerieEscolar.PRIMEIRA_SERIE));

		mockMvc.perform(delete("/api/disciplinas/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/disciplinas/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoComIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(delete("/api/disciplinas/{id}", -1)).andExpect(status().isNotFound());
	}

	private ResultActions enviar(MockHttpServletRequestBuilder requisicao, DisciplinaRequest corpo)
			throws Exception {
		return mockMvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(corpo)));
	}

	private Long cadastrar(DisciplinaRequest request) throws Exception {
		String corpo = enviar(post("/api/disciplinas"), request)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(corpo, "$.id")).longValue();
	}

}
