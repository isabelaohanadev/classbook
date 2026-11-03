package br.com.classbook.desktop.modelo;

import java.time.LocalDate;

/**
 * Material digital devolvido por {@code /api/materiais}, com a disciplina resumida. Espelha o
 * {@code MaterialDigitalResponse} do backend.
 */
public record MaterialDigitalResponse(Long id, String titulo, FormatoMaterial formato, String url,
		String descricao, LocalDate dataPublicacao, DisciplinaResumo disciplina) {

	public record DisciplinaResumo(Long id, String nome, String codigo) {
	}

}
