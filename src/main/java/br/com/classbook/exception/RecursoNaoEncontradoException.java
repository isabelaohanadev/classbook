package br.com.classbook.exception;

/**
 * O {@code id} informado na rota não corresponde a nenhum registro (ver docs/ARQUITETURA.md). O
 * tratador global converte esta exceção em {@code 404 Not Found}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

}
