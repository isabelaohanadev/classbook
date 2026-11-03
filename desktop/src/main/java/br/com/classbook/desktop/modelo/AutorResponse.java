package br.com.classbook.desktop.modelo;

import java.time.LocalDate;

/** Autor devolvido por {@code /api/autores}. Espelha o {@code AutorResponse} do backend. */
public record AutorResponse(Long id, String nome, String nacionalidade, LocalDate dataNascimento, String biografia) {
}
