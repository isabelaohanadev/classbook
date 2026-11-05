package br.com.classbook.desktop.modelo;

/** Formato de um material digital, igual ao enum do backend. */
public enum FormatoMaterial {

	PDF("PDF"),
	EPUB("EPUB"),
	VIDEO("Vídeo"),
	AUDIO("Áudio"),
	LINK("Link"),
	OUTRO("Outro");

	private final String rotulo;

	FormatoMaterial(String rotulo) {
		this.rotulo = rotulo;
	}

	/** O nome para mostrar na interface. O JSON continua usando o {@code name()}, como a API espera. */
	public String rotulo() {
		return rotulo;
	}

}
