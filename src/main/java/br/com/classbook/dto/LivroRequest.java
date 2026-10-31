package br.com.classbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpo do {@code POST} e do {@code PUT} de {@code /api/livros}. Os tamanhos máximos são os das
 * colunas da tabela {@code livro}. A categoria é ligada na Issue #26.
 */
public record LivroRequest(
		@NotBlank @Size(max = 200) String titulo,
		@NotBlank @Pattern(regexp = "^(\\d{9}[\\dX]|\\d{13})$") String isbn,
		Integer anoPublicacao,
		String descricao,
		@Size(max = 120) String editora,
		@NotNull Long autorId) {

	public LivroRequest {
		// Roda antes do @Valid: o @Pattern confere o ISBN já sem hífens e em maiúsculas.
		isbn = isbn == null ? null : isbn.replaceAll("[-\\s]", "").toUpperCase();
	}

}
