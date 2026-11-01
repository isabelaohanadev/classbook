package br.com.classbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Título e descrição da documentação OpenAPI (Issue #47). As rotas vêm dos próprios controllers, lidas
 * pelo springdoc: o Swagger UI fica em {@code /swagger-ui.html} e o JSON em {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI classbookOpenApi() {
		return new OpenAPI().info(new Info()
				.title("ClassBook API")
				.description("API REST da biblioteca digital de uma escola de ensino médio: cadastro e consulta de "
						+ "autores, livros, categorias, disciplinas e materiais digitais, com pesquisa de livros por "
						+ "título. Os erros seguem um formato JSON único, descrito no README.")
				.version("0.0.1")
				.license(new License().name("MIT")));
	}

}
