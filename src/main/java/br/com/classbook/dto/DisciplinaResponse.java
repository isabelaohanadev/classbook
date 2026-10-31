package br.com.classbook.dto;

import br.com.classbook.model.Disciplina;
import br.com.classbook.model.SerieEscolar;

/**
 * Saída de todas as rotas de {@code /api/disciplinas}.
 */
public record DisciplinaResponse(Long id, String nome, String codigo, String descricao, SerieEscolar serieEscolar) {

	public static DisciplinaResponse de(Disciplina disciplina) {
		return new DisciplinaResponse(disciplina.getId(), disciplina.getNome(), disciplina.getCodigo(),
				disciplina.getDescricao(), disciplina.getSerieEscolar());
	}

}
