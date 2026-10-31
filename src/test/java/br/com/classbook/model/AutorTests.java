package br.com.classbook.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere o mapeamento de {@link Autor} para a tabela {@code autor} da migration inicial. Que a
 * aplicação inicia com {@code ddl-auto=validate} já é conferido ao subir o contexto.
 *
 * <p>Cada teste roda em uma transação desfeita ao final, então o banco não fica com dados.
 */
@SpringBootTest
@Transactional
class AutorTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void gravaELeTodosOsCampos() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		autor.setNacionalidade("Brasileira");
		autor.setDataNascimento(LocalDate.of(1839, 6, 21));
		autor.setBiografia("Romancista, contista e poeta brasileiro.");
		entityManager.persist(autor);
		entityManager.clear();

		Autor lido = entityManager.find(Autor.class, autor.getId());

		assertThat(lido.getNome()).isEqualTo("Machado de Assis");
		assertThat(lido.getNacionalidade()).isEqualTo("Brasileira");
		assertThat(lido.getDataNascimento()).isEqualTo(LocalDate.of(1839, 6, 21));
		assertThat(lido.getBiografia()).isEqualTo("Romancista, contista e poeta brasileiro.");
	}

	@Test
	void camposOpcionaisPodemFicarNulos() {
		Autor autor = new Autor();
		autor.setNome("Autor Sem Dados Extras");
		entityManager.persist(autor);
		entityManager.clear();

		Autor lido = entityManager.find(Autor.class, autor.getId());

		assertThat(lido.getNacionalidade()).isNull();
		assertThat(lido.getDataNascimento()).isNull();
		assertThat(lido.getBiografia()).isNull();
	}

}
