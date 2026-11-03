package br.com.classbook.desktop.modelo;

/**
 * Livro devolvido por {@code /api/livros}, com o autor e a categoria resumidos (a categoria pode ser
 * {@code null}). Espelha o {@code LivroResponse} do backend.
 */
public record LivroResponse(Long id, String titulo, String isbn, Integer anoPublicacao, String descricao,
		String editora, AutorResumo autor, CategoriaResumo categoria) {

	public record AutorResumo(Long id, String nome) {
	}

	public record CategoriaResumo(Long id, String nome) {
	}

}
