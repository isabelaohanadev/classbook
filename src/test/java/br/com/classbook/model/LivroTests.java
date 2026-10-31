package br.com.classbook.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere o mapeamento de {@link Livro} para a tabela {@code livro} da migration inicial, inclusive o
 * relacionamento obrigatório com {@link Autor}. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class LivroTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void gravaELeTodosOsCamposEOAutor() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		entityManager.persist(autor);

		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAnoPublicacao(1899);
		livro.setDescricao("Romance narrado por Bentinho");
		livro.setEditora("Editora Exemplo");
		livro.setAutor(autor);
		entityManager.persist(livro);
		entityManager.clear();

		Livro lido = entityManager.find(Livro.class, livro.getId());

		assertThat(lido.getTitulo()).isEqualTo("Dom Casmurro");
		assertThat(lido.getIsbn()).isEqualTo("9780306406157");
		assertThat(lido.getAnoPublicacao()).isEqualTo(1899);
		assertThat(lido.getDescricao()).isEqualTo("Romance narrado por Bentinho");
		assertThat(lido.getEditora()).isEqualTo("Editora Exemplo");
		assertThat(lido.getAutor().getId()).isEqualTo(autor.getId());
	}

	@Test
	void camposOpcionaisPodemFicarNulos() {
		Autor autor = new Autor();
		autor.setNome("Autor Qualquer");
		entityManager.persist(autor);

		Livro livro = new Livro();
		livro.setTitulo("Livro Sem Extras");
		livro.setIsbn("9780306406157");
		livro.setAutor(autor);
		entityManager.persist(livro);
		entityManager.clear();

		Livro lido = entityManager.find(Livro.class, livro.getId());

		assertThat(lido.getAnoPublicacao()).isNull();
		assertThat(lido.getDescricao()).isNull();
		assertThat(lido.getEditora()).isNull();
	}

}
