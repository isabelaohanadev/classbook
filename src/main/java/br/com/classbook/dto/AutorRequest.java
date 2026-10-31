package br.com.classbook.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

/**
 * Corpo do {@code POST} e do {@code PUT} de {@code /api/autores}. Os tamanhos máximos são os das
 * colunas da tabela {@code autor}.
 */
public record AutorRequest(
		@NotBlank @Size(max = 150) String nome,
		@Size(max = 80) String nacionalidade,
		@Past LocalDate dataNascimento,
		String biografia) {

}
