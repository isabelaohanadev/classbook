package br.com.classbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpo do {@code POST} e do {@code PUT} de {@code /api/livros}. Os tamanhos máximos são os das
 * colunas da tabela {@code livro}. {@code categoriaId} e {@code disciplinaId} são opcionais: ausente ou
 * {@code null} deixa (ou limpa, no {@code PUT}) o livro sem categoria ou sem disciplina.
 */
public record LivroRequest(
		@NotBlank @Size(max = 200) String titulo,
		@NotBlank @Pattern(regexp = "^(\\d{9}[\\dX]|\\d{13})$",
				message = "deve ter 10 ou 13 dígitos, sem contar hífens e espaços (o ISBN-10 pode terminar em X)")
		String isbn,
		Integer anoPublicacao,
		String descricao,
		@Size(max = 120) String editora,
		@NotNull Long autorId,
		Long categoriaId,
		Long disciplinaId) {

	public LivroRequest {
		// Roda antes do @Valid: o @Pattern confere o ISBN já sem hífens e em maiúsculas.
		isbn = isbn == null ? null : isbn.replaceAll("[-\\s]", "").toUpperCase();
	}

}
