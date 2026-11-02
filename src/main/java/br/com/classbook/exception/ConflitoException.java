package br.com.classbook.exception;

/**
 * Duplicidade (ISBN, nome de categoria, código de disciplina) ou exclusão de um registro ainda
 * referenciado, como um autor com livros. O {@link TratadorGlobalDeExcecoes} responde {@code 409} com
 * esta mensagem (ver docs/ARQUITETURA.md#erros-e-exceções).
 */
public class ConflitoException extends RuntimeException {

	public ConflitoException(String mensagem) {
		super(mensagem);
	}

}
