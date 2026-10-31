package br.com.classbook.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.LivroResponse.AutorResumo;
import br.com.classbook.model.Autor;
import br.com.classbook.model.Livro;

/**
 * Confere que {@link LivroResponse#de(Livro)} copia todos os campos da entidade, inclusive o resumo do
 * autor. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class LivroResponseTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void deCopiaTodosOsCamposDaEntidadeEOResumoDoAutor() {
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
				"Descrição", "Editora Exemplo", new AutorResumo(autor.getId(), "Machado de Assis")));
	}

}
