package br.com.classbook.dto;

import java.time.LocalDate;

import br.com.classbook.model.Disciplina;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.MaterialDigital;

/**
 * Saída de todas as rotas de {@code /api/materiais}. A disciplina vem resumida com o {@code codigo},
 * porque o nome não é único (decidido no PR #83).
 */
public record MaterialDigitalResponse(Long id, String titulo, FormatoMaterial formato, String url,
		String descricao, LocalDate dataPublicacao, DisciplinaResumo disciplina) {

	public record DisciplinaResumo(Long id, String nome, String codigo) {
	}

	public static MaterialDigitalResponse de(MaterialDigital material) {
		Disciplina disciplina = material.getDisciplina();
		return new MaterialDigitalResponse(material.getId(), material.getTitulo(), material.getFormato(),
				material.getUrl(), material.getDescricao(), material.getDataPublicacao(),
				new DisciplinaResumo(disciplina.getId(), disciplina.getNome(), disciplina.getCodigo()));
	}

}
