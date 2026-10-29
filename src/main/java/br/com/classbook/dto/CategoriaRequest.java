package br.com.classbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.com.classbook.model.TipoCategoria;

/**
 * Corpo do {@code POST} e do {@code PUT} de {@code /api/categorias}. Os tamanhos máximos são os das
 * colunas da tabela {@code categoria}.
 */
public record CategoriaRequest(
		@NotBlank @Size(max = 100) String nome,
		@Size(max = 500) String descricao,
		@Size(max = 30) String faixaEtaria,
		@NotNull TipoCategoria tipo) {

	public CategoriaRequest {
		// Roda antes do @Valid: a validação, o service e o banco recebem o nome sem espaços nas pontas.
		nome = nome == null ? null : nome.strip();
	}

}
