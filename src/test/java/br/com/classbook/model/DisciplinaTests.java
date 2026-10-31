package br.com.classbook.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere o mapeamento de {@link Disciplina} para a tabela {@code disciplina} da migration inicial.
 * Que a aplicação inicia com {@code ddl-auto=validate} já é conferido ao subir o contexto.
 *
 * <p>Cada teste roda em uma transação desfeita ao final, então o banco não fica com dados.
 */
@SpringBootTest
@Transactional
class DisciplinaTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void gravaELeTodosOsCampos() {
		Long id = persistir("Matemática", "MAT-1", "Matemática da 1ª série", SerieEscolar.PRIMEIRA_SERIE);
		entityManager.clear();

		Disciplina lida = entityManager.find(Disciplina.class, id);

		assertThat(lida.getNome()).isEqualTo("Matemática");
		assertThat(lida.getCodigo()).isEqualTo("MAT-1");
		assertThat(lida.getDescricao()).isEqualTo("Matemática da 1ª série");
		assertThat(lida.getSerieEscolar()).isEqualTo(SerieEscolar.PRIMEIRA_SERIE);
	}

	@Test
	void serieEscolarEhGravadaPeloNomeDaConstante() {
		Long id = persistir("Física", "FIS-3", null, SerieEscolar.TERCEIRA_SERIE);

		String serie = jdbc.queryForObject("SELECT serie_escolar FROM disciplina WHERE id = ?", String.class, id);

		assertThat(serie).isEqualTo("TERCEIRA_SERIE");
	}

	private Long persistir(String nome, String codigo, String descricao, SerieEscolar serieEscolar) {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome(nome);
		disciplina.setCodigo(codigo);
		disciplina.setDescricao(descricao);
		disciplina.setSerieEscolar(serieEscolar);
		entityManager.persist(disciplina);
		entityManager.flush();
		return disciplina.getId();
	}

}
