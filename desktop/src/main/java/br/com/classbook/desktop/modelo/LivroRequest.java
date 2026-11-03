package br.com.classbook.desktop.modelo;

/**
 * Corpo do {@code POST} e do {@code PUT} em {@code /api/livros}; a categoria é opcional. Espelha o
 * {@code LivroRequest} do backend.
 */
public record LivroRequest(String titulo, String isbn, Integer anoPublicacao, String descricao, String editora,
		Long autorId, Long categoriaId) {
}
