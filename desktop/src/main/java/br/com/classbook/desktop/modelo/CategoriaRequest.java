package br.com.classbook.desktop.modelo;

/** Corpo do {@code POST} e do {@code PUT} em {@code /api/categorias}. Espelha o {@code CategoriaRequest} do backend. */
public record CategoriaRequest(String nome, String descricao, String faixaEtaria, TipoCategoria tipo) {
}
