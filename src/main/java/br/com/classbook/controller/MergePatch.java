package br.com.classbook.controller;

/**
 * Constantes do {@code PATCH} dos cinco controllers: o tipo de conteúdo do JSON Merge Patch (RFC 7396),
 * além do {@code application/json} comum, e o texto das regras que o Swagger mostra em cada rota.
 */
final class MergePatch {

	static final String CONTENT_TYPE = "application/merge-patch+json";

	static final String REGRAS = "Altera só os campos enviados (JSON Merge Patch, RFC 7396): campo ausente não "
			+ "muda, e null limpa o valor de um campo opcional. O resultado passa pelas mesmas regras do PUT, "
			+ "então um campo obrigatório com null, um valor inválido ou um duplicado dão o mesmo erro.";

	private MergePatch() {
	}

}
