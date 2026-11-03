package br.com.classbook.desktop.modelo;

/** Disciplina devolvida por {@code /api/disciplinas}. Espelha o {@code DisciplinaResponse} do backend. */
public record DisciplinaResponse(Long id, String nome, String codigo, String descricao, SerieEscolar serieEscolar) {
}
