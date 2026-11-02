package br.com.classbook.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.SerieEscolar;
import br.com.classbook.repository.DisciplinaRepository;
import br.com.classbook.repository.MaterialDigitalRepository;

/**
 * Confere o relacionamento Material→Disciplina de ponta a ponta <b>sem</b> transação de teste: cada
 * requisição abre e fecha a própria transação, como em produção. Com {@code open-in-view=false} e a
 * disciplina {@code LAZY}, uma conversão para DTO fora do service lançaria
 * {@code LazyInitializationException} aqui, o que os testes {@code @Transactional} não conseguem pegar.
 *
 * <p>Os registros criados são apagados ao final de cada teste, e o código de cada disciplina é único,
 * para não depender do que já existe no banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MaterialDigitalRelacionamentoTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MaterialDigitalRepository materialDigitalRepository;

	@Autowired
	private DisciplinaRepository disciplinaRepository;

	private final List<Long> materiaisCriados = new ArrayList<>();

	private final List<Long> disciplinasCriadas = new ArrayList<>();

	@AfterEach
	void apagarOsRegistrosCriados() {
		materialDigitalRepository.deleteAllById(materiaisCriados);
		disciplinaRepository.deleteAllById(disciplinasCriadas);
	}

	@Test
	void listagemConsultaEEdicaoTrazemADisciplinaResumidaForaDaTransacao() throws Exception {
		String codigo = codigoUnico();
		Long disciplinaId = cadastrarDisciplina(codigo);
		Long materialId = cadastrarMaterial(disciplinaId);

		String lista = mockMvc.perform(get("/api/materiais"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		List<String> codigos = JsonPath.read(lista, "$[?(@.id == " + materialId + ")].disciplina.codigo");
		assertThat(codigos).containsExactly(codigo);

		mockMvc.perform(get("/api/materiais/{id}", materialId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.disciplina.nome").value("Matemática"))
				.andExpect(jsonPath("$.disciplina.codigo").value(codigo));

		enviar(put("/api/materiais/{id}", materialId), new MaterialDigitalRequest("Lista revisada",
				FormatoMaterial.PDF, "https://exemplo.com/revisada.pdf", null, null, disciplinaId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.disciplina.codigo").value(codigo));
	}

	@Test
	void disciplinaComMateriaisSoPodeSerExcluidaDepoisDosMateriais() throws Exception {
		Long disciplinaId = cadastrarDisciplina(codigoUnico());
		Long materialId = cadastrarMaterial(disciplinaId);

		mockMvc.perform(delete("/api/disciplinas/{id}", disciplinaId)).andExpect(status().isConflict());
		assertThat(disciplinaRepository.existsById(disciplinaId)).isTrue();

		mockMvc.perform(delete("/api/materiais/{id}", materialId)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/disciplinas/{id}", disciplinaId)).andExpect(status().isOk());

		mockMvc.perform(delete("/api/disciplinas/{id}", disciplinaId)).andExpect(status().isNoContent());
		assertThat(disciplinaRepository.existsById(disciplinaId)).isFalse();
	}

	private static String codigoUnico() {
		return "REL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
	}

	private Long cadastrarDisciplina(String codigo) throws Exception {
		Long id = cadastrar("/api/disciplinas",
				new DisciplinaRequest("Matemática", codigo, null, SerieEscolar.PRIMEIRA_SERIE));
		disciplinasCriadas.add(id);
		return id;
	}

	private Long cadastrarMaterial(Long disciplinaId) throws Exception {
		Long id = cadastrar("/api/materiais", new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF,
				"https://exemplo.com/funcoes.pdf", null, null, disciplinaId));
		materiaisCriados.add(id);
		return id;
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
