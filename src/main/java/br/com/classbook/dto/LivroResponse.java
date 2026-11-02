package br.com.classbook.dto;

import br.com.classbook.model.Categoria;
import br.com.classbook.model.Livro;

/**
 * Saída de todas as rotas de {@code /api/livros}. {@code categoria} vem {@code null} quando o livro não
 * tem categoria.
 */
public record LivroResponse(Long id, String titulo, String isbn, Integer anoPublicacao, String descricao,
		String editora, AutorResumo autor, CategoriaResumo categoria) {

	public record AutorResumo(Long id, String nome) {
	}

	public record CategoriaResumo(Long id, String nome) {
	}

	public static LivroResponse de(Livro livro) {
		Categoria categoria = livro.getCategoria();
		return new LivroResponse(livro.getId(), livro.getTitulo(), livro.getIsbn(), livro.getAnoPublicacao(),
				livro.getDescricao(), livro.getEditora(),
				new AutorResumo(livro.getAutor().getId(), livro.getAutor().getNome()),
				categoria == null ? null : new CategoriaResumo(categoria.getId(), categoria.getNome()));
	}

}
