package br.com.classbook.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.model.TipoCategoria;

/**
 * Confere as rotas de {@code /api/categorias} de ponta a ponta, com o contexto Spring completo e o
 * PostgreSQL de teste. A transação é desfeita ao final de cada teste, e nenhum teste supõe que o banco
 * começa vazio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void cadastroValidoRespondeCreatedComLocationEACategoria() throws Exception {
		CategoriaRequest request = new CategoriaRequest("Literatura", "Romances, contos e poesia", "14 a 17 anos",
				TipoCategoria.LITERATURA);

		MvcResult resultado = enviar(post("/api/categorias"), request)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Literatura"))
				.andExpect(jsonPath("$.descricao").value("Romances, contos e poesia"))
				.andExpect(jsonPath("$.faixaEtaria").value("14 a 17 anos"))
				.andExpect(jsonPath("$.tipo").value("LITERATURA"))
				.andReturn();

		Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
		assertThat(resultado.getResponse().getHeader("Location")).endsWith("/api/categorias/" + id.longValue());
	}

	@Test
	void cadastroSemNomeOuTipoRespondeBadRequest() throws Exception {
		enviar(post("/api/categorias"), new CategoriaRequest("   ", null, null, null))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComNomeJaUsadoRespondeConflict() throws Exception {
		cadastrar(new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA));

		enviar(post("/api/categorias"), new CategoriaRequest("  literatura ", null, null, TipoCategoria.OUTRO))
				.andExpect(status().isConflict());
	}

	@Test
	void listagemRespondeOkComCategoriasOrdenadasPorNome() throws Exception {
		cadastrar(new CategoriaRequest("Referência", null, null, TipoCategoria.REFERENCIA));
		cadastrar(new CategoriaRequest("Didáticos", null, null, TipoCategoria.DIDATICO));
		cadastrar(new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA));

		String corpo = mockMvc.perform(get("/api/categorias"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		List<String> nomes = JsonPath.read(corpo, "$[*].nome");
		assertThat(nomes).containsSubsequence("Didáticos", "Literatura", "Referência");
	}

	@Test
	void consultaPorIdExistenteRespondeOkComACategoria() throws Exception {
		Long id = cadastrar(new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA));

		mockMvc.perform(get("/api/categorias/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.nome").value("Literatura"));
	}

	@Test
	void consultaPorIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(get("/api/categorias/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void edicaoValidaRespondeOkComACategoriaAtualizada() throws Exception {
		Long id = cadastrar(new CategoriaRequest("Nome Antigo", "Descrição antiga", null, TipoCategoria.OUTRO));

		enviar(put("/api/categorias/{id}", id),
				new CategoriaRequest("Nome Novo", null, "15 a 17 anos", TipoCategoria.PARADIDATICO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Nome Novo"))
				.andExpect(jsonPath("$.descricao").value(nullValue()))
				.andExpect(jsonPath("$.faixaEtaria").value("15 a 17 anos"))
				.andExpect(jsonPath("$.tipo").value("PARADIDATICO"));
	}

	@Test
	void edicaoComIdInexistenteRespondeNotFound() throws Exception {
		enviar(put("/api/categorias/{id}", -1), new CategoriaRequest("Nome", null, null, TipoCategoria.OUTRO))
				.andExpect(status().isNotFound());
	}

	@Test
	void edicaoComDadosInvalidosRespondeBadRequest() throws Exception {
		Long id = cadastrar(new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA));

		enviar(put("/api/categorias/{id}", id), new CategoriaRequest("Literatura", null, null, null))
				.andExpect(status().isBadRequest());
	}

	@Test
	void edicaoComNomeDeOutraCategoriaRespondeConflict() throws Exception {
		cadastrar(new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA));
		Long id = cadastrar(new CategoriaRequest("Referência", null, null, TipoCategoria.REFERENCIA));

		CategoriaRequest edicao = new CategoriaRequest("LITERATURA", null, null, TipoCategoria.REFERENCIA);

		enviar(put("/api/categorias/{id}", id), edicao)
				.andExpect(status().isConflict());
	}

	@Test
	void edicaoQueSoMudaAsMaiusculasDoProprioNomeRespondeOk() throws Exception {
		Long id = cadastrar(new CategoriaRequest("literatura", null, null, TipoCategoria.LITERATURA));

		CategoriaRequest edicao = new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA);

		enviar(put("/api/categorias/{id}", id), edicao)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Literatura"));
	}

	@Test
	void exclusaoDeCategoriaExistenteRespondeNoContent() throws Exception {
		Long id = cadastrar(new CategoriaRequest("Categoria Descartável", null, null, TipoCategoria.OUTRO));

		mockMvc.perform(delete("/api/categorias/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/categorias/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoComIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(delete("/api/categorias/{id}", -1)).andExpect(status().isNotFound());
	}

	private ResultActions enviar(MockHttpServletRequestBuilder requisicao, CategoriaRequest corpo) throws Exception {
		return mockMvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(corpo)));
	}

	private Long cadastrar(CategoriaRequest request) throws Exception {
		String corpo = enviar(post("/api/categorias"), request)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(corpo, "$.id")).longValue();
	}

}
