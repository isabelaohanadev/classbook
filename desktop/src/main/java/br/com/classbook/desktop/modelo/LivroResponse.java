package br.com.classbook.desktop.modelo;

/**
 * Livro devolvido por {@code /api/livros}, com o autor, a categoria e a disciplina resumidos (a categoria e
 * a disciplina podem ser {@code null}). Espelha o {@code LivroResponse} do backend.
 */
public record LivroResponse(Long id, String titulo, String isbn, Integer anoPublicacao, String descricao,
		String editora, AutorResumo autor, CategoriaResumo categoria, DisciplinaResumo disciplina) {

	public record AutorResumo(Long id, String nome) {
	}

	public record CategoriaResumo(Long id, String nome) {
	}

	public record DisciplinaResumo(Long id, String nome, String codigo) {
	}

}
