package br.com.classbook.dto;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Disciplina;
import br.com.classbook.model.SerieEscolar;

/**
 * Confere que {@link DisciplinaResponse#de(Disciplina)} copia todos os campos da entidade, inclusive o
 * {@code id} gerado pelo banco. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class DisciplinaResponseTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void deCopiaTodosOsCamposDaEntidade() {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Matemática");
		disciplina.setCodigo(codigo("MAT-1"));
		disciplina.setDescricao("Matemática da 1ª série");
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);
		entityManager.persist(disciplina);

		DisciplinaResponse response = DisciplinaResponse.de(disciplina);

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new DisciplinaResponse(disciplina.getId(), "Matemática", codigo("MAT-1"),
				"Matemática da 1ª série", SerieEscolar.PRIMEIRA_SERIE));
	}

}
