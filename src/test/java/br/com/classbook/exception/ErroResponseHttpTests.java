package br.com.classbook.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Confere o formato de erro com um servidor HTTP de verdade. O MockMvc não passa pelo Tomcat nem pela
 * página de erro do Spring Boot ({@code /error}); este teste garante que o cliente recebe o
 * {@link ErroResponse}, e não o corpo padrão do Spring Boot, que não trazia a mensagem.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErroResponseHttpTests {

	@LocalServerPort
	private int porta;

	@Test
	void erroDeDominioChegaAoClienteComAMensagem() throws Exception {
		HttpResponse<String> resposta = get("/api/autores/-1");

		assertThat(resposta.statusCode()).isEqualTo(404);
		assertThat(JsonPath.<String>read(resposta.body(), "$.mensagem")).isEqualTo("Autor -1 não encontrado.");
		assertThat(resposta.headers().firstValue("Content-Type")).hasValueSatisfying(
				tipo -> assertThat(tipo).startsWith("application/json"));
	}

	@Test
	void rotaInexistenteTambemSaiNoFormatoPadrao() throws Exception {
		HttpResponse<String> resposta = get("/api/inexistente");

		assertThat(resposta.statusCode()).isEqualTo(404);
		assertThat(JsonPath.<String>read(resposta.body(), "$.mensagem")).isEqualTo("Rota não encontrada.");
	}

	private HttpResponse<String> get(String caminho) throws Exception {
		HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).build();
		try (HttpClient cliente = HttpClient.newHttpClient()) {
			return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
		}
	}

}
