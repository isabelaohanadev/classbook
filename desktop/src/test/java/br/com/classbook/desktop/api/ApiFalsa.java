package br.com.classbook.desktop.api;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpServer;

/**
 * Servidor HTTP local, numa porta livre, que faz o papel da API nos testes: guarda as requisições recebidas
 * e devolve a resposta programada para a rota ({@link #responder(String, int, String)}) ou, sem ela, a
 * resposta padrão ({@link #responder(int, String)}).
 */
public class ApiFalsa implements AutoCloseable {

	/** Uma requisição recebida; {@code rota} é o método e o caminho, como em "POST /api/autores". */
	public record Requisicao(String rota, String corpo) {
	}

	private record Resposta(int status, String corpo) {
	}

	private final HttpServer servidor;
	private final Map<String, Resposta> respostasPorRota = new ConcurrentHashMap<>();
	private final List<Requisicao> recebidas = new CopyOnWriteArrayList<>();
	private volatile Resposta respostaPadrao = new Resposta(200, "");

	volatile String metodo;
	volatile String caminho;
	volatile String consulta;
	volatile String tipoDoConteudo;
	volatile String corpoRecebido;

	public ApiFalsa() throws IOException {
		servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		servidor.createContext("/", troca -> {
			metodo = troca.getRequestMethod();
			caminho = troca.getRequestURI().getPath();
			consulta = troca.getRequestURI().getRawQuery();
			tipoDoConteudo = troca.getRequestHeaders().getFirst("Content-Type");
			corpoRecebido = new String(troca.getRequestBody().readAllBytes(), UTF_8);
			recebidas.add(new Requisicao(metodo + " " + caminho, corpoRecebido));
			Resposta resposta = respostasPorRota.getOrDefault(metodo + " " + caminho, respostaPadrao);
			byte[] corpo = resposta.corpo().getBytes(UTF_8);
			troca.getResponseHeaders().add("Content-Type", "application/json");
			troca.sendResponseHeaders(resposta.status(), corpo.length == 0 ? -1 : corpo.length);
			if (corpo.length > 0) {
				troca.getResponseBody().write(corpo);
			}
			troca.close();
		});
		servidor.start();
	}

	public void responder(int status, String corpo) {
		respostaPadrao = new Resposta(status, corpo);
	}

	/** Programa a resposta de uma rota, como {@code responder("GET /api/autores", 200, "[]")}. */
	public void responder(String rota, int status, String corpo) {
		respostasPorRota.put(rota, new Resposta(status, corpo));
	}

	public List<Requisicao> recebidas() {
		return List.copyOf(recebidas);
	}

	/** As requisições que alteram dados ({@code POST}, {@code PUT} e {@code DELETE}). */
	public List<Requisicao> alteracoes() {
		return recebidas.stream().filter(requisicao -> !requisicao.rota().startsWith("GET ")).toList();
	}

	public String url() {
		return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/api";
	}

	@Override
	public void close() {
		servidor.stop(0);
	}

}
