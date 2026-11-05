package br.com.classbook.desktop.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import br.com.classbook.desktop.modelo.AutorRequest;
import br.com.classbook.desktop.modelo.AutorResponse;
import br.com.classbook.desktop.modelo.CategoriaRequest;
import br.com.classbook.desktop.modelo.CategoriaResponse;
import br.com.classbook.desktop.modelo.DisciplinaRequest;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.LivroRequest;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.MaterialDigitalRequest;
import br.com.classbook.desktop.modelo.MaterialDigitalResponse;

/**
 * Cliente HTTP da API REST do ClassBook (Issue #59), com {@code java.net.http.HttpClient} e Jackson.
 * Cada recurso tem as cinco operações em {@link Recurso}, e os livros têm também a pesquisa com filtros.
 *
 * <p>A URL base vem de {@link #urlConfigurada()}. As chamadas bloqueiam até a resposta: a interface deve
 * fazê-las por {@code EmSegundoPlano}, fora da thread do JavaFX.
 */
public class ClienteApi {

	public static final String URL_PADRAO = "http://localhost:8080/api";

	private static final Duration TEMPO_PARA_CONECTAR = Duration.ofSeconds(5);
	private static final Duration TEMPO_PARA_RESPONDER = Duration.ofSeconds(15);

	private final String urlBase;
	private final HttpClient http;
	private final JsonMapper json;

	public ClienteApi() {
		this(urlConfigurada());
	}

	public ClienteApi(String urlBase) {
		this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
		this.http = HttpClient.newBuilder().connectTimeout(TEMPO_PARA_CONECTAR).build();
		// Um campo novo na API não deve quebrar a interface, que só lê os campos que conhece.
		this.json = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
	}

	/**
	 * A propriedade de sistema {@code classbook.api.url}; sem ela, a variável de ambiente
	 * {@code CLASSBOOK_API_URL}; sem as duas, {@value #URL_PADRAO}.
	 */
	public static String urlConfigurada() {
		String propriedade = System.getProperty("classbook.api.url");
		if (propriedade != null && !propriedade.isBlank()) {
			return propriedade.strip();
		}
		String variavel = System.getenv("CLASSBOOK_API_URL");
		return variavel != null && !variavel.isBlank() ? variavel.strip() : URL_PADRAO;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public Recurso<AutorRequest, AutorResponse> autores() {
		return new Recurso<>(this, "/autores", AutorResponse.class);
	}

	public Recurso<LivroRequest, LivroResponse> livros() {
		return new Recurso<>(this, "/livros", LivroResponse.class);
	}

	public Recurso<CategoriaRequest, CategoriaResponse> categorias() {
		return new Recurso<>(this, "/categorias", CategoriaResponse.class);
	}

	public Recurso<DisciplinaRequest, DisciplinaResponse> disciplinas() {
		return new Recurso<>(this, "/disciplinas", DisciplinaResponse.class);
	}

	public Recurso<MaterialDigitalRequest, MaterialDigitalResponse> materiais() {
		return new Recurso<>(this, "/materiais", MaterialDigitalResponse.class);
	}

	/**
	 * {@code GET /livros} com os filtros informados; {@code null} ou em branco fica de fora da pesquisa. A
	 * {@code busca} é o texto livre: o backend a procura no título, no nome do autor, no nome da categoria e
	 * no ano de publicação.
	 */
	public List<LivroResponse> pesquisarLivros(String busca, Long autorId, Long categoriaId) {
		List<String> filtros = new ArrayList<>();
		if (busca != null && !busca.isBlank()) {
			filtros.add("busca=" + URLEncoder.encode(busca.strip(), StandardCharsets.UTF_8).replace("+", "%20"));
		}
		if (autorId != null) {
			filtros.add("autorId=" + autorId);
		}
		if (categoriaId != null) {
			filtros.add("categoriaId=" + categoriaId);
		}
		String consulta = filtros.isEmpty() ? "" : "?" + String.join("&", filtros);
		return lista("GET", "/livros" + consulta, LivroResponse.class);
	}

	<T> List<T> lista(String metodo, String caminho, Class<T> tipo) {
		String corpo = enviar(metodo, caminho, null);
		return json.readValue(corpo, json.getTypeFactory().constructCollectionType(List.class, tipo));
	}

	<T> T objeto(String metodo, String caminho, Object request, Class<T> tipo) {
		return json.readValue(enviar(metodo, caminho, request), tipo);
	}

	/** Envia a requisição e devolve o corpo da resposta de sucesso, ou lança a exceção do erro. */
	String enviar(String metodo, String caminho, Object request) {
		HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(urlBase + caminho))
				.timeout(TEMPO_PARA_RESPONDER)
				.header("Accept", "application/json");
		if (request == null) {
			requisicao.method(metodo, BodyPublishers.noBody());
		} else {
			requisicao.header("Content-Type", "application/json")
					.method(metodo, BodyPublishers.ofString(json.writeValueAsString(request), StandardCharsets.UTF_8));
		}
		HttpResponse<String> resposta;
		try {
			resposta = http.send(requisicao.build(), BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (IOException erro) {
			throw new ApiIndisponivelException(urlBase, erro);
		} catch (InterruptedException erro) {
			Thread.currentThread().interrupt();
			throw new ApiIndisponivelException(urlBase, erro);
		}
		if (resposta.statusCode() >= 400) {
			throw erroDa(resposta);
		}
		return resposta.body();
	}

	private ErroDaApiException erroDa(HttpResponse<String> resposta) {
		try {
			ErroResponse erro = json.readValue(resposta.body(), ErroResponse.class);
			if (erro.mensagem() != null && !erro.mensagem().isBlank()) {
				return new ErroDaApiException(resposta.statusCode(), erro.mensagem(),
						erro.campos() == null ? List.of() : erro.campos());
			}
		} catch (JacksonException corpoQueNaoEhErroResponse) {
			// Segue para a mensagem genérica abaixo (por exemplo, uma página HTML de um proxy).
		}
		return new ErroDaApiException(resposta.statusCode(),
				"A API respondeu com o código " + resposta.statusCode() + ".", List.of());
	}

	/** O corpo de erro da API (o {@code ErroResponse} do backend), só com o que a interface usa. */
	private record ErroResponse(String mensagem, List<ErroDaApiException.CampoInvalido> campos) {
	}

}
