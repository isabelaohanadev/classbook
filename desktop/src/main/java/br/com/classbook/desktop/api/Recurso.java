package br.com.classbook.desktop.api;

import java.util.List;

/**
 * As cinco operações de um recurso da API ({@code /autores}, {@code /livros} etc.). {@code Q} é o
 * {@code Request} enviado no cadastro e na edição, e {@code R} é o {@code Response} devolvido.
 *
 * <p>Todos os métodos bloqueiam até a resposta chegar: na interface, chame-os por
 * {@code EmSegundoPlano}, nunca direto na thread do JavaFX. Um código de erro vira
 * {@link ErroDaApiException}, e a falta de resposta vira {@link ApiIndisponivelException}.
 */
public class Recurso<Q, R> {

	private final ClienteApi cliente;
	private final String caminho;
	private final Class<R> tipoDaResposta;

	Recurso(ClienteApi cliente, String caminho, Class<R> tipoDaResposta) {
		this.cliente = cliente;
		this.caminho = caminho;
		this.tipoDaResposta = tipoDaResposta;
	}

	public List<R> listar() {
		return cliente.lista("GET", caminho, tipoDaResposta);
	}

	public R buscar(long id) {
		return cliente.objeto("GET", caminho + "/" + id, null, tipoDaResposta);
	}

	public R cadastrar(Q request) {
		return cliente.objeto("POST", caminho, request, tipoDaResposta);
	}

	public R editar(long id, Q request) {
		return cliente.objeto("PUT", caminho + "/" + id, request, tipoDaResposta);
	}

	public void excluir(long id) {
		cliente.enviar("DELETE", caminho + "/" + id, null);
	}

}
