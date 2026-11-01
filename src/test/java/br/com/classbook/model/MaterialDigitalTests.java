package br.com.classbook.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere o mapeamento de {@link MaterialDigital} para a tabela {@code material_digital} da migration
 * inicial, inclusive o relacionamento obrigatório com {@link Disciplina}. A transação é desfeita ao
 * final.
 */
@SpringBootTest
@Transactional
class MaterialDigitalTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void gravaELeTodosOsCamposEADisciplina() {
		Disciplina disciplina = persistirDisciplina();
		MaterialDigital material = new MaterialDigital();
		material.setTitulo("Lista de exercícios de funções");
		material.setFormato(FormatoMaterial.PDF);
		material.setUrl("https://exemplo.com/funcoes.pdf");
		material.setDescricao("Exercícios resolvidos");
		material.setDataPublicacao(LocalDate.of(2026, 3, 10));
		material.setDisciplina(disciplina);
		entityManager.persist(material);
		entityManager.flush();
		entityManager.clear();

		MaterialDigital lido = entityManager.find(MaterialDigital.class, material.getId());

		assertThat(lido.getTitulo()).isEqualTo("Lista de exercícios de funções");
		assertThat(lido.getFormato()).isEqualTo(FormatoMaterial.PDF);
		assertThat(lido.getUrl()).isEqualTo("https://exemplo.com/funcoes.pdf");
		assertThat(lido.getDescricao()).isEqualTo("Exercícios resolvidos");
		assertThat(lido.getDataPublicacao()).isEqualTo(LocalDate.of(2026, 3, 10));
		assertThat(lido.getDisciplina().getId()).isEqualTo(disciplina.getId());
	}

	@Test
	void camposOpcionaisPodemFicarNulosEOFormatoEhGravadoPeloNome() {
		MaterialDigital material = new MaterialDigital();
		material.setTitulo("Videoaula");
		material.setFormato(FormatoMaterial.VIDEO);
		material.setUrl("https://exemplo.com/video");
		material.setDisciplina(persistirDisciplina());
		entityManager.persist(material);
		entityManager.flush();

		String formato = jdbc.queryForObject("SELECT formato FROM material_digital WHERE id = ?", String.class,
				material.getId());
		entityManager.clear();
		MaterialDigital lido = entityManager.find(MaterialDigital.class, material.getId());

		assertThat(formato).isEqualTo("VIDEO");
		assertThat(lido.getDescricao()).isNull();
		assertThat(lido.getDataPublicacao()).isNull();
	}

	private Disciplina persistirDisciplina() {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Matemática");
		disciplina.setCodigo("MAT-1");
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);
		entityManager.persist(disciplina);
		return disciplina;
	}

}
