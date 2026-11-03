package br.com.classbook.desktop.api;

/**
 * Não houve resposta da API: o backend está fora do ar, a URL base está errada ou a conexão demorou
 * demais. A mensagem diz onde o cliente tentou chegar, sem detalhes técnicos.
 */
public class ApiIndisponivelException extends RuntimeException {

	public ApiIndisponivelException(String urlBase, Throwable causa) {
		super("Não foi possível falar com a API em " + urlBase + ". Confira se o backend está rodando.", causa);
	}

}
