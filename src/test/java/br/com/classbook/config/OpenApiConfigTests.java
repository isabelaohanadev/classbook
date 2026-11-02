package br.com.classbook.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Confere que a documentação OpenAPI é gerada com o título da API e as rotas dos controllers, e que o
 * Swagger UI é servido.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTests {

	@Autowired
	private MockMvc mockMvc;

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

}
