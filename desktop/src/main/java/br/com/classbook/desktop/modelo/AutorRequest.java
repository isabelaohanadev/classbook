package br.com.classbook.desktop.modelo;

import java.time.LocalDate;

/** Corpo do {@code POST} e do {@code PUT} em {@code /api/autores}. Espelha o {@code AutorRequest} do backend. */
public record AutorRequest(String nome, String nacionalidade, LocalDate dataNascimento, String biografia) {
}
