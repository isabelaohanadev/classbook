package br.com.classbook.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import br.com.classbook.model.FormatoMaterial;

/**
 * Corpo do {@code POST} e do {@code PUT} de {@code /api/materiais}. Os tamanhos máximos são os das
 * colunas da tabela {@code material_digital}.
 */
public record MaterialDigitalRequest(
		@NotBlank @Size(max = 200) String titulo,
		@NotNull FormatoMaterial formato,
		// @URL confere a sintaxe; o regexp restringe a http/https, sem espaços.
		@NotBlank @Size(max = 500)
		@URL(regexp = "^https?://\\S+$", flags = Pattern.Flag.CASE_INSENSITIVE) String url,
		String descricao,
		LocalDate dataPublicacao,
		@NotNull Long disciplinaId) {

}
