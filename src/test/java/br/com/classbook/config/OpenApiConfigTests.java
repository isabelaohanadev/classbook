package br.com.classbook.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Confere que a documentação OpenAPI é gerada com o título da API e as rotas dos controllers, que o
 * Swagger UI é servido e que as rotas já documentadas (Issue #48) têm resumo e respostas descritas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTests {

	/** Rotas com resumo e respostas documentados. As de Autor e Livro entram quando forem documentadas. */
	private static final String ROTAS_DOCUMENTADAS = "/api/(categorias|disciplinas|materiais)(/\\{id\\})?";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void apiDocsDevolveOJsonComTituloDescricaoERotas() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("ClassBook API"))
				.andExpect(jsonPath("$.info.description").isNotEmpty())
				.andExpect(jsonPath("$.paths['/api/autores'].post").exists())
				.andExpect(jsonPath("$.paths['/api/livros/{id}'].put").exists());
	}

	@Test
	void swaggerUiEhServido() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}

	@Test
	void rotasDocumentadasTemTagResumoERespostasDescritasComErrosNoFormatoErroResponse() throws Exception {
		String json = mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		List<String> operacoes = new ArrayList<>();
		for (Map.Entry<String, JsonNode> rota : objectMapper.readTree(json).path("paths").properties()) {
			if (!rota.getKey().matches(ROTAS_DOCUMENTADAS)) {
				continue;
			}
			for (Map.Entry<String, JsonNode> metodo : rota.getValue().properties()) {
				String operacao = metodo.getKey().toUpperCase(Locale.ROOT) + " " + rota.getKey();
				JsonNode documentacao = metodo.getValue();
				assertThat(documentacao.path("summary").asString()).as(operacao).isNotBlank();
				assertThat(documentacao.path("tags").path(0).asString()).as(operacao)
						.isIn("Categorias", "Disciplinas", "Materiais digitais");
				for (Map.Entry<String, JsonNode> resposta : documentacao.path("responses").properties()) {
					String codigo = operacao + " → " + resposta.getKey();
					// "OK", "Created" e "No Content" são as descrições que o springdoc põe sem a anotação.
					assertThat(resposta.getValue().path("description").asString()).as(codigo)
							.isNotBlank().isNotIn("OK", "Created", "No Content");
					if (resposta.getKey().startsWith("4")) {
						assertThat(resposta.getValue().at("/content/application~1json/schema/$ref").asString())
								.as(codigo).isEqualTo("#/components/schemas/ErroResponse");
					}
				}
				operacoes.add(operacao);
			}
		}
		assertThat(operacoes).hasSize(15);
	}

	@Test
	void esquemaDoErroResponseApareceNosComponentes() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.schemas.ErroResponse.properties.mensagem").exists())
				.andExpect(jsonPath("$.components.schemas.ErroResponse.properties.campos.items['$ref']")
						.value("#/components/schemas/CampoInvalido"))
				.andExpect(jsonPath("$.components.schemas.CampoInvalido.properties.campo").exists());
	}

}
