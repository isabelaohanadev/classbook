package br.com.classbook.desktop.modelo;

import java.time.LocalDate;

/**
 * Corpo do {@code POST} e do {@code PUT} em {@code /api/materiais}. Espelha o {@code MaterialDigitalRequest}
 * do backend.
 */
public record MaterialDigitalRequest(String titulo, FormatoMaterial formato, String url, String descricao,
		LocalDate dataPublicacao, Long disciplinaId) {
}
