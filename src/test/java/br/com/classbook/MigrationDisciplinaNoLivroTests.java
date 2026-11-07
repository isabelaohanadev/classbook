package br.com.classbook;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere as regras da migration {@code V202611051836__adiciona_disciplina_em_livro.sql}, que o Hibernate não
 * valida com {@code ddl-auto=validate}: a coluna {@code livro.disciplina_id} é opcional e a chave
 * estrangeira bloqueia (ON DELETE RESTRICT) a exclusão de uma disciplina que ainda tem livros (ver
 * docs/MODELAGEM.md).
 *
 * <p>Cada teste roda em uma transação desfeita ao final, então o banco não fica com dados.
 */
@SpringBootTest
@Transactional
class MigrationDisciplinaNoLivroTests {

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void flywayAplicaAMigrationDaDisciplinaNoLivro() {
		Boolean sucesso = jdbc.queryForObject(
				"SELECT success FROM flyway_schema_history WHERE version = '202611051836'", Boolean.class);

		assertThat(sucesso).isTrue();
	}

	@Test
	void disciplinaDoLivroEhOpcional() {
		String anulavel = jdbc.queryForObject("""
				SELECT is_nullable FROM information_schema.columns
				WHERE table_schema = current_schema() AND table_name = 'livro' AND column_name = 'disciplina_id'
				""", String.class);

		assertThat(anulavel).isEqualTo("YES");
		assertThatCode(() -> inserirLivro("0306406152", inserirAutor(), null)).doesNotThrowAnyException();
	}

	@Test
	void livroExigeDisciplinaExistenteQuandoInformada() {
		long autorId = inserirAutor();

		assertThatThrownBy(() -> inserirLivro("0306406152", autorId, -1L))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("fk_livro_disciplina");
	}

	@Test
	void naoExcluiDisciplinaComLivros() {
		long disciplinaId = inserirDisciplina(codigo("LP-1"));
		inserirLivro("0306406152", inserirAutor(), disciplinaId);

		assertThatThrownBy(() -> jdbc.update("DELETE FROM disciplina WHERE id = ?", disciplinaId))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("fk_livro_disciplina");
	}

	@Test
	void excluirOLivroNaoAfetaADisciplina() {
		long disciplinaId = inserirDisciplina(codigo("LP-1"));
		long livroId = inserirLivro("0306406152", inserirAutor(), disciplinaId);

		jdbc.update("DELETE FROM livro WHERE id = ?", livroId);

		assertThat(jdbc.queryForObject("SELECT count(*) FROM disciplina WHERE id = ?", Long.class, disciplinaId))
				.isEqualTo(1L);
	}

	@Test
	void existeIndiceNaChaveEstrangeiraDaDisciplina() {
		Integer indices = jdbc.queryForObject("""
				SELECT count(*) FROM pg_indexes
				WHERE schemaname = current_schema() AND tablename = 'livro'
				  AND indexname = 'idx_livro_disciplina_id'
				""", Integer.class);

		assertThat(indices).isEqualTo(1);
	}

	private long inserirAutor() {
		return jdbc.queryForObject("INSERT INTO autor (nome) VALUES ('Autor') RETURNING id", Long.class);
	}

	private long inserirDisciplina(String codigo) {
		return jdbc.queryForObject("""
				INSERT INTO disciplina (nome, codigo, serie_escolar)
				VALUES ('Língua Portuguesa', ?, 'PRIMEIRA_SERIE') RETURNING id
				""", Long.class, codigo);
	}

	private long inserirLivro(String isbn, long autorId, Long disciplinaId) {
		return jdbc.queryForObject("""
				INSERT INTO livro (titulo, isbn, autor_id, disciplina_id)
				VALUES ('Livro de teste', ?, ?, ?) RETURNING id
				""", Long.class, isbn, autorId, disciplinaId);
	}

}
