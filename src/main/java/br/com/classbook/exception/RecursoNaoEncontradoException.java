package br.com.classbook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * O {@code id} informado na rota não corresponde a nenhum registro (ver docs/ARQUITETURA.md).
 *
 * <p>O tratador global da Issue #43 (Etapa 4) vai padronizar o corpo de erro; até lá, o
 * {@code @ResponseStatus} já garante o {@code 404} via {@code ResponseStatusExceptionResolver} do
 * próprio Spring, sem precisar desse tratador. Com {@code server.error.include-message=always}, a
 * mensagem desta exceção chega ao cliente no corpo padrão de erro do Spring Boot.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

}
