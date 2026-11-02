package br.com.classbook.controller;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.jayway.jsonpath.JsonPath;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
		DisciplinaRequest request = new DisciplinaRequest("Matemática", " " + codigo("mat-1") + " ",
				"Matemática da 1ª série",
				SerieEscolar.PRIMEIRA_SERIE);

		MvcResult resultado = enviar(post("/api/disciplinas"), request)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Matemática"))
				.andExpect(jsonPath("$.codigo").value(codigo("MAT-1")))
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
		cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(post("/api/disciplinas"), new DisciplinaRequest("Outra", codigo("mat-1"), null,
				SerieEscolar.SEGUNDA_SERIE))
				.andExpect(status().isConflict());
	}

	@Test
	void listagemRespondeOkComDisciplinasOrdenadasPorCodigo() throws Exception {
		cadastrar(new DisciplinaRequest("Química", codigo("QUI-2"), null, SerieEscolar.SEGUNDA_SERIE));
		cadastrar(new DisciplinaRequest("Física", codigo("FIS-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-3"), null, SerieEscolar.TERCEIRA_SERIE));

		String corpo = mockMvc.perform(get("/api/disciplinas"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		List<String> codigos = JsonPath.read(corpo, "$[*].codigo");
		assertThat(codigos).containsSubsequence(codigo("FIS-1"), codigo("MAT-3"), codigo("QUI-2"));
	}

	@Test
	void consultaPorIdExistenteRespondeOkComADisciplina() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		mockMvc.perform(get("/api/disciplinas/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.codigo").value(codigo("MAT-1")));
	}

	@Test
	void consultaPorIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(get("/api/disciplinas/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void edicaoValidaRespondeOkComADisciplinaAtualizada() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Nome Antigo", codigo("ANT-1"), "Descrição antiga",
				SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Nome Novo", codigo("nov-2"), null,
				SerieEscolar.SEGUNDA_SERIE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Nome Novo"))
				.andExpect(jsonPath("$.codigo").value(codigo("NOV-2")))
				.andExpect(jsonPath("$.descricao").value(nullValue()))
				.andExpect(jsonPath("$.serieEscolar").value("SEGUNDA_SERIE"));
	}

	@Test
	void edicaoComIdInexistenteRespondeNotFound() throws Exception {
		enviar(put("/api/disciplinas/{id}", -1), new DisciplinaRequest("Nome", codigo("COD-1"), null,
				SerieEscolar.PRIMEIRA_SERIE))
				.andExpect(status().isNotFound());
	}

	@Test
	void edicaoComDadosInvalidosRespondeBadRequest() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Matemática", codigo("MAT-1"), null, null))
				.andExpect(status().isBadRequest());
	}

	@Test
	void edicaoComCodigoDeOutraDisciplinaRespondeConflict() throws Exception {
		cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		Long id = cadastrar(new DisciplinaRequest("Física", codigo("FIS-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Física", codigo("MAT-1"), null,
				SerieEscolar.PRIMEIRA_SERIE))
				.andExpect(status().isConflict());
	}

	@Test
	void edicaoMantendoOProprioCodigoRespondeOk() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		enviar(put("/api/disciplinas/{id}", id), new DisciplinaRequest("Matemática Básica", codigo("mat-1"), null,
				SerieEscolar.PRIMEIRA_SERIE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Matemática Básica"));
	}

	@Test
	void exclusaoDeDisciplinaSemMateriaisRespondeNoContent() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Descartável", codigo("DESC-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		mockMvc.perform(delete("/api/disciplinas/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/disciplinas/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoComIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(delete("/api/disciplinas/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoDeDisciplinaComMateriaisVinculadosRespondeConflict() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		enviar(post("/api/materiais"), new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF,
				"https://exemplo.com/funcoes.pdf", null, null, id))
				.andExpect(status().isCreated());

		mockMvc.perform(delete("/api/disciplinas/{id}", id)).andExpect(status().isConflict());
		mockMvc.perform(get("/api/disciplinas/{id}", id)).andExpect(status().isOk());
	}

	@Test
	void respostaDaDisciplinaNaoTrazCampoDeMateriais() throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		mockMvc.perform(get("/api/disciplinas/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.materiais").doesNotExist());
	}

	@Test
	void corpoBaseDaMatrizDeValidacaoEhValido() throws Exception {
		enviar(post("/api/disciplinas"), corpoValido()).andExpect(status().isCreated());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("camposInvalidos")
	void cadastroEEdicaoComCampoInvalidoRespondemBadRequest(String caso, String campo, Object valor)
			throws Exception {
		Long id = cadastrar(new DisciplinaRequest("Física", codigo("FIS-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		Map<String, Object> corpo = corpoValido();
		corpo.put(campo, valor);

		enviar(post("/api/disciplinas"), corpo).andExpect(status().isBadRequest());
		enviar(put("/api/disciplinas/{id}", id), corpo).andExpect(status().isBadRequest());
	}

	static Stream<Arguments> camposInvalidos() {
		return Stream.of(
				Arguments.of("nome ausente", "nome", null),
				Arguments.of("nome em branco", "nome", "   "),
				Arguments.of("nome maior que a coluna", "nome", "a".repeat(101)),
				Arguments.of("código ausente", "codigo", null),
				Arguments.of("código em branco", "codigo", "   "),
				Arguments.of("código maior que a coluna", "codigo", "A".repeat(21)),
				Arguments.of("descrição maior que a coluna", "descricao", "a".repeat(501)),
				Arguments.of("série ausente", "serieEscolar", null),
				Arguments.of("série fora da lista", "serieEscolar", "QUARTA_SERIE"),
				Arguments.of("série em minúsculas", "serieEscolar", "primeira_serie"));
	}

	/** Corpo válido em que cada caso da matriz troca um único campo. */
	private static Map<String, Object> corpoValido() {
		return new HashMap<>(Map.of("nome", "Matemática", "codigo", codigo("MAT-1"), "descricao", "1ª série",
				"serieEscolar", "PRIMEIRA_SERIE"));
	}

	private ResultActions enviar(MockHttpServletRequestBuilder requisicao, Object corpo) throws Exception {
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
