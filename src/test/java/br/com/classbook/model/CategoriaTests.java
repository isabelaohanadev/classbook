package br.com.classbook.model;

import static br.com.classbook.Unico.nome;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere o mapeamento de {@link Categoria} para a tabela {@code categoria} da migration inicial.
 * Que a aplicação inicia com {@code ddl-auto=validate} já é conferido ao subir o contexto.
 *
 * <p>Cada teste roda em uma transação desfeita ao final, então o banco não fica com dados.
 */
@SpringBootTest
@Transactional
class CategoriaTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void gravaELeTodosOsCampos() {
		Long id = persistir(nome("Literatura"), "Romances, contos e poesia", "14 a 17 anos", TipoCategoria.LITERATURA);
		entityManager.clear();

		Categoria lida = entityManager.find(Categoria.class, id);

		assertThat(lida.getNome()).isEqualTo(nome("Literatura"));
		assertThat(lida.getDescricao()).isEqualTo("Romances, contos e poesia");
		assertThat(lida.getFaixaEtaria()).isEqualTo("14 a 17 anos");
		assertThat(lida.getTipo()).isEqualTo(TipoCategoria.LITERATURA);
	}

	@Test
	void tipoEhGravadoPeloNomeDaConstante() {
		Long id = persistir(nome("Dicionários"), null, null, TipoCategoria.REFERENCIA);

		String tipo = jdbc.queryForObject("SELECT tipo FROM categoria WHERE id = ?", String.class, id);

		assertThat(tipo).isEqualTo("REFERENCIA");
	}

	private Long persistir(String nome, String descricao, String faixaEtaria, TipoCategoria tipo) {
		Categoria categoria = new Categoria();
		categoria.setNome(nome);
		categoria.setDescricao(descricao);
		categoria.setFaixaEtaria(faixaEtaria);
		categoria.setTipo(tipo);
		entityManager.persist(categoria);
		entityManager.flush();
		return categoria.getId();
	}

}
