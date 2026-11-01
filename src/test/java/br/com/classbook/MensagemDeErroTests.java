package br.com.classbook;

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
 * Confere, com um servidor HTTP de verdade, que a mensagem de uma exceção de domínio chega no corpo
 * padrão de erro do Spring Boot. O MockMvc não passa pela página de erro ({@code /error}), então só um
 * teste assim percebe se a propriedade {@code spring.web.error.include-message} deixar de valer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MensagemDeErroTests {

	@LocalServerPort
	private int porta;

	@Test
	void mensagemDaExcecaoDeDominioChegaNoCorpoDoErro() throws Exception {
		HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/api/autores/-1"))
				.build();

		HttpResponse<String> resposta;
		try (HttpClient cliente = HttpClient.newHttpClient()) {
			resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
		}

		assertThat(resposta.statusCode()).isEqualTo(404);
		assertThat(JsonPath.<String>read(resposta.body(), "$.message")).isEqualTo("Autor -1 não encontrado.");
	}

}
