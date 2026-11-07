package br.com.classbook.desktop.modelo;

/**
 * Corpo do {@code POST} e do {@code PUT} em {@code /api/livros}; a categoria e a disciplina são
 * opcionais (no {@code PUT}, {@code null} remove a ligação). Espelha o {@code LivroRequest} do backend.
 */
public record LivroRequest(String titulo, String isbn, Integer anoPublicacao, String descricao, String editora,
		Long autorId, Long categoriaId, Long disciplinaId) {
}
