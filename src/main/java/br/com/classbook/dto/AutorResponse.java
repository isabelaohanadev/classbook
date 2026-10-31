package br.com.classbook.dto;

import java.time.LocalDate;

import br.com.classbook.model.Autor;

/**
 * Saída de todas as rotas de {@code /api/autores}.
 */
public record AutorResponse(Long id, String nome, String nacionalidade, LocalDate dataNascimento, String biografia) {

	public static AutorResponse de(Autor autor) {
		return new AutorResponse(autor.getId(), autor.getNome(), autor.getNacionalidade(),
				autor.getDataNascimento(), autor.getBiografia());
	}

}
