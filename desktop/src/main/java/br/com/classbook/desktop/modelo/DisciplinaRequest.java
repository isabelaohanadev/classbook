package br.com.classbook.desktop.modelo;

/**
 * Corpo do {@code POST} e do {@code PUT} em {@code /api/disciplinas}. Espelha o {@code DisciplinaRequest} do
 * backend.
 */
public record DisciplinaRequest(String nome, String codigo, String descricao, SerieEscolar serieEscolar) {
}
