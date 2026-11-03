package br.com.classbook.desktop.api;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

/**
 * Servidor HTTP local, numa porta livre, que faz o papel da API nos testes: guarda a última requisição
 * recebida e devolve a resposta programada com {@link #responder(int, String)}.
 */
class ApiFalsa implements AutoCloseable {

	private final HttpServer servidor;
	private volatile int status = 200;
	private volatile String corpoDaResposta = "";

	volatile String metodo;
	volatile String caminho;
	volatile String consulta;
	volatile String tipoDoConteudo;
	volatile String corpoRecebido;

	ApiFalsa() throws IOException {
		servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		servidor.createContext("/", troca -> {
			metodo = troca.getRequestMethod();
			caminho = troca.getRequestURI().getPath();
			consulta = troca.getRequestURI().getRawQuery();
			tipoDoConteudo = troca.getRequestHeaders().getFirst("Content-Type");
			corpoRecebido = new String(troca.getRequestBody().readAllBytes(), UTF_8);
			byte[] corpo = corpoDaResposta.getBytes(UTF_8);
			troca.getResponseHeaders().add("Content-Type", "application/json");
			troca.sendResponseHeaders(status, corpo.length == 0 ? -1 : corpo.length);
			if (corpo.length > 0) {
				troca.getResponseBody().write(corpo);
			}
			troca.close();
		});
		servidor.start();
	}

	void responder(int status, String corpo) {
		this.status = status;
		this.corpoDaResposta = corpo;
	}

	String url() {
		return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/api";
	}

	@Override
	public void close() {
		servidor.stop(0);
	}

}
