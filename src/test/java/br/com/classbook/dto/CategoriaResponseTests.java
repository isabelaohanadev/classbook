package br.com.classbook.dto;

import static br.com.classbook.Unico.nome;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Categoria;
import br.com.classbook.model.TipoCategoria;

/**
 * Confere que {@link CategoriaResponse#de(Categoria)} copia todos os campos da entidade, inclusive o
 * {@code id} gerado pelo banco. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class CategoriaResponseTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void deCopiaTodosOsCamposDaEntidade() {
		Categoria categoria = new Categoria();
		categoria.setNome(nome("Literatura"));
		categoria.setDescricao("Romances, contos e poesia");
		categoria.setFaixaEtaria("14 a 17 anos");
		categoria.setTipo(TipoCategoria.LITERATURA);
		entityManager.persist(categoria);

		CategoriaResponse response = CategoriaResponse.de(categoria);

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new CategoriaResponse(categoria.getId(), nome("Literatura"),
				"Romances, contos e poesia", "14 a 17 anos", TipoCategoria.LITERATURA));
	}

}
