package br.com.classbook.dto;

import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.com.classbook.model.SerieEscolar;

/**
 * Corpo do {@code POST} e do {@code PUT} de {@code /api/disciplinas}. Os tamanhos máximos são os das
 * colunas da tabela {@code disciplina}.
 */
public record DisciplinaRequest(
		@NotBlank @Size(max = 100) String nome,
		@NotBlank @Size(max = 20) String codigo,
		@Size(max = 500) String descricao,
		@NotNull SerieEscolar serieEscolar) {

	public DisciplinaRequest {
		// Roda antes do @Valid: a validação, o service e o banco recebem o código sem espaços nas pontas e
		// em maiúsculas. Locale.ROOT evita que o "i" vire "İ" numa JVM em turco.
		codigo = codigo == null ? null : codigo.strip().toUpperCase(Locale.ROOT);
	}

}
