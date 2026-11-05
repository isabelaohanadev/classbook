package br.com.classbook.desktop.modelo;

/** Tipo de uma categoria de livros, igual ao enum do backend. */
public enum TipoCategoria {

	DIDATICO("Didático"),
	PARADIDATICO("Paradidático"),
	LITERATURA("Literatura"),
	REFERENCIA("Referência"),
	OUTRO("Outro");

	private final String rotulo;

	TipoCategoria(String rotulo) {
		this.rotulo = rotulo;
	}

	/** O nome para mostrar na interface. O JSON continua usando o {@code name()}, como a API espera. */
	public String rotulo() {
		return rotulo;
	}

}
