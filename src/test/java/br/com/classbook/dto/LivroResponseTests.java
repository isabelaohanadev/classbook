package br.com.classbook.dto;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.LivroResponse.AutorResumo;
import br.com.classbook.dto.LivroResponse.CategoriaResumo;
import br.com.classbook.dto.LivroResponse.DisciplinaResumo;
import br.com.classbook.model.Autor;
import br.com.classbook.model.Categoria;
import br.com.classbook.model.Disciplina;
import br.com.classbook.model.Livro;
import br.com.classbook.model.SerieEscolar;
import br.com.classbook.model.TipoCategoria;

/**
 * Confere que {@link LivroResponse#de(Livro)} copia todos os campos da entidade, inclusive os resumos do
 * autor, da categoria e da disciplina. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class LivroResponseTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void deCopiaTodosOsCamposDaEntidadeEOResumoDoAutorSemCategoriaNemDisciplina() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		entityManager.persist(autor);

		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAnoPublicacao(1899);
		livro.setDescricao("Descrição");
		livro.setEditora("Editora Exemplo");
		livro.setAutor(autor);
		entityManager.persist(livro);

		LivroResponse response = LivroResponse.de(livro);

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new LivroResponse(livro.getId(), "Dom Casmurro", "9780306406157", 1899,
				"Descrição", "Editora Exemplo", new AutorResumo(autor.getId(), "Machado de Assis"), null, null));
	}

	@Test
	void deCopiaOResumoDaCategoriaQuandoOLivroTemCategoria() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		entityManager.persist(autor);

		Categoria categoria = new Categoria();
		categoria.setNome("Literatura");
		categoria.setTipo(TipoCategoria.LITERATURA);
		entityManager.persist(categoria);

		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAutor(autor);
		livro.setCategoria(categoria);
		entityManager.persist(livro);

		LivroResponse response = LivroResponse.de(livro);

		assertThat(response.categoria()).isEqualTo(new CategoriaResumo(categoria.getId(), "Literatura"));
	}

	@Test
	void deCopiaOResumoDaDisciplinaComOCodigoQuandoOLivroTemDisciplina() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		entityManager.persist(autor);

		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Língua Portuguesa");
		disciplina.setCodigo(codigo("LP-1"));
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);
		entityManager.persist(disciplina);

		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAutor(autor);
		livro.setDisciplina(disciplina);
		entityManager.persist(livro);

		LivroResponse response = LivroResponse.de(livro);

		assertThat(response.disciplina())
				.isEqualTo(new DisciplinaResumo(disciplina.getId(), "Língua Portuguesa", codigo("LP-1")));
		assertThat(response.categoria()).isNull();
	}

}
