package br.com.classbook.dto;

import br.com.classbook.model.Livro;

/**
 * Saída de todas as rotas de {@code /api/livros}. A categoria é ligada na Issue #26.
 */
public record LivroResponse(Long id, String titulo, String isbn, Integer anoPublicacao, String descricao,
		String editora, AutorResumo autor) {

	public record AutorResumo(Long id, String nome) {
	}

	public static LivroResponse de(Livro livro) {
		return new LivroResponse(livro.getId(), livro.getTitulo(), livro.getIsbn(), livro.getAnoPublicacao(),
				livro.getDescricao(), livro.getEditora(),
				new AutorResumo(livro.getAutor().getId(), livro.getAutor().getNome()));
	}

}
