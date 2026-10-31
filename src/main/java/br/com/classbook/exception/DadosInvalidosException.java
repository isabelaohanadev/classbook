package br.com.classbook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Regra de entrada conferida no service, que depende do banco e por isso não cabe no
 * {@code @Valid} do controller: por exemplo, {@code autorId} inexistente (ver
 * docs/ARQUITETURA.md#erros-e-exceções).
 *
 * <p>Ver nota de {@link RecursoNaoEncontradoException} sobre o {@code @ResponseStatus} como solução
 * provisória até o tratador global da Issue #43.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DadosInvalidosException extends RuntimeException {

	public DadosInvalidosException(String mensagem) {
		super(mensagem);
	}

}
