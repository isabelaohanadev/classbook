package br.com.classbook.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Autor;

/**
 * Confere que {@link AutorResponse#de(Autor)} copia todos os campos da entidade, inclusive o
 * {@code id} gerado pelo banco. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class AutorResponseTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void deCopiaTodosOsCamposDaEntidade() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		autor.setNacionalidade("Brasileira");
		autor.setDataNascimento(LocalDate.of(1839, 6, 21));
		autor.setBiografia("Romancista, contista e poeta brasileiro.");
		entityManager.persist(autor);

		AutorResponse response = AutorResponse.de(autor);

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new AutorResponse(autor.getId(), "Machado de Assis", "Brasileira",
				LocalDate.of(1839, 6, 21), "Romancista, contista e poeta brasileiro."));
	}

}
