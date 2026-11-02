package br.com.classbook.exception;

/**
 * O {@code id} informado na rota não corresponde a nenhum registro. O {@link TratadorGlobalDeExcecoes}
 * responde {@code 404} com esta mensagem (ver docs/ARQUITETURA.md#erros-e-exceções).
 */
public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

}
