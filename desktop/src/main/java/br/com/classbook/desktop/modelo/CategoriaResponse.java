package br.com.classbook.desktop.modelo;

/** Categoria devolvida por {@code /api/categorias}. Espelha o {@code CategoriaResponse} do backend. */
public record CategoriaResponse(Long id, String nome, String descricao, String faixaEtaria, TipoCategoria tipo) {
}
