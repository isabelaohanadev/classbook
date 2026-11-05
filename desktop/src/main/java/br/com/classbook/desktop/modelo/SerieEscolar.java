package br.com.classbook.desktop.modelo;

/** Série do ensino médio de uma disciplina, igual ao enum do backend. */
public enum SerieEscolar {

	PRIMEIRA_SERIE("1ª série"),
	SEGUNDA_SERIE("2ª série"),
	TERCEIRA_SERIE("3ª série");

	private final String rotulo;

	SerieEscolar(String rotulo) {
		this.rotulo = rotulo;
	}

	/** O nome para mostrar na interface. O JSON continua usando o {@code name()}, como a API espera. */
	public String rotulo() {
		return rotulo;
	}

}
