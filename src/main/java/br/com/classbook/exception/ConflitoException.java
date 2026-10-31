package br.com.classbook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Duplicidade (ISBN, nome de categoria, código de disciplina) ou exclusão de um registro ainda
 * referenciado, como um autor com livros (ver docs/ARQUITETURA.md#erros-e-exceções).
 *
 * <p>Ver nota de {@link RecursoNaoEncontradoException} sobre o {@code @ResponseStatus} como solução
 * provisória até o tratador global da Issue #43.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflitoException extends RuntimeException {

	public ConflitoException(String mensagem) {
		super(mensagem);
	}

}
