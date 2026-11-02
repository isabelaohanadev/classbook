package br.com.classbook.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.classbook.exception.ErroResponse;

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

	/**
	 * Toda resposta 4xx documentada numa rota passa a mostrar o corpo {@link ErroResponse}, que é o que o
	 * {@code TratadorGlobalDeExcecoes} devolve. Assim os controllers só declaram o código e a descrição de
	 * cada erro (Issue #48); sem isso, o springdoc mostraria o tipo de retorno do método também nos erros.
	 */
	@Bean
	public OpenApiCustomizer respostasDeErroComErroResponse() {
		return openApi -> {
			ModelConverters.getInstance().readAll(ErroResponse.class).forEach(openApi.getComponents()::addSchemas);
			Content corpoDeErro = new Content().addMediaType("application/json",
					new MediaType().schema(new Schema<ErroResponse>().$ref("ErroResponse")));
			openApi.getPaths().values().stream()
					.flatMap(rota -> rota.readOperations().stream())
					.flatMap(operacao -> operacao.getResponses().entrySet().stream())
					.filter(resposta -> resposta.getKey().startsWith("4"))
					.forEach(resposta -> resposta.getValue().setContent(corpoDeErro));
		};
	}

}
