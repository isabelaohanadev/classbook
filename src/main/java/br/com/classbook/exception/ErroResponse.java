package br.com.classbook.exception;

import java.time.Instant;
import java.util.List;

/**
 * Corpo de todas as respostas de erro da API (Issue #44): o mesmo formato em todas as rotas, com
 * mensagem em português e sem stack trace nem detalhes internos. {@code campos} lista os campos
 * inválidos de um {@code 400} e vem vazio nos demais erros.
 */
public record ErroResponse(Instant horario, int status, String erro, String mensagem, String caminho,
		List<CampoInvalido> campos) {

	public record CampoInvalido(String campo, String mensagem) {
	}

}
