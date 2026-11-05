package br.com.classbook.dto;

import br.com.classbook.model.Categoria;
import br.com.classbook.model.Disciplina;
import br.com.classbook.model.Livro;

/**
 * Saída de todas as rotas de {@code /api/livros}. {@code categoria} e {@code disciplina} vêm
 * {@code null} quando o livro não tem categoria ou disciplina. A disciplina vem resumida com o
 * {@code codigo}, porque o nome não é único (como em {@code MaterialDigitalResponse}).
 */
public record LivroResponse(Long id, String titulo, String isbn, Integer anoPublicacao, String descricao,
		String editora, AutorResumo autor, CategoriaResumo categoria, DisciplinaResumo disciplina) {

	public record AutorResumo(Long id, String nome) {
	}

	public record CategoriaResumo(Long id, String nome) {
	}

	public record DisciplinaResumo(Long id, String nome, String codigo) {
	}

	public static LivroResponse de(Livro livro) {
		Categoria categoria = livro.getCategoria();
		Disciplina disciplina = livro.getDisciplina();
		return new LivroResponse(livro.getId(), livro.getTitulo(), livro.getIsbn(), livro.getAnoPublicacao(),
				livro.getDescricao(), livro.getEditora(),
				new AutorResumo(livro.getAutor().getId(), livro.getAutor().getNome()),
				categoria == null ? null : new CategoriaResumo(categoria.getId(), categoria.getNome()),
				disciplina == null ? null
						: new DisciplinaResumo(disciplina.getId(), disciplina.getNome(), disciplina.getCodigo()));
	}

}
