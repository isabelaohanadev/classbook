package br.com.classbook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere as regras que a migration inicial declara e que o Hibernate não valida com
 * {@code ddl-auto=validate}: {@code NOT NULL}, {@code UNIQUE} e chaves estrangeiras com
 * {@code ON DELETE RESTRICT} (ver docs/MODELAGEM.md).
 *
 * <p>Cada teste roda em uma transação desfeita ao final, então o banco não fica com dados.
 */
@SpringBootTest
@Transactional
class MigrationInicialTests {

	@Autowired
	private JdbcTemplate jdbc;

	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String ddlAuto;

	@Test
	void flywayAplicaAMigrationInicial() {
		Boolean sucesso = jdbc.queryForObject(
				"SELECT success FROM flyway_schema_history WHERE version = '1'", Boolean.class);

		assertThat(sucesso).isTrue();
	}

	@Test
	void hibernateApenasValidaOSchema() {
		assertThat(ddlAuto).isEqualTo("validate");
	}

	@Test
	void camposObrigatoriosSaoNotNull() {
		List<String> obrigatorios = jdbc.queryForList("""
				SELECT table_name || '.' || column_name
				FROM information_schema.columns
				WHERE table_schema = current_schema()
				  AND table_name IN ('autor', 'livro', 'categoria', 'disciplina', 'material_digital')
				  AND is_nullable = 'NO'
				""", String.class);

		assertThat(obrigatorios).containsExactlyInAnyOrder(
				"autor.id", "autor.nome",
				"livro.id", "livro.titulo", "livro.isbn", "livro.autor_id",
				"categoria.id", "categoria.nome", "categoria.tipo",
				"disciplina.id", "disciplina.nome", "disciplina.codigo", "disciplina.serie_escolar",
				"material_digital.id", "material_digital.titulo", "material_digital.formato",
				"material_digital.url", "material_digital.disciplina_id");
	}

	@Test
	void isbnEhUnico() {
		long autorId = inserirAutor("Autor");
		inserirLivro("9780306406157", autorId, null);

		assertThatThrownBy(() -> inserirLivro("9780306406157", autorId, null))
				.isInstanceOf(DuplicateKeyException.class)
				.hasMessageContaining("uk_livro_isbn");
	}

	@Test
	void nomeDeCategoriaEhUnicoSemDiferenciarMaiusculas() {
		inserirCategoria("Literatura");

		assertThatThrownBy(() -> inserirCategoria("literatura"))
				.isInstanceOf(DuplicateKeyException.class)
				.hasMessageContaining("uk_categoria_nome");
	}

	@Test
	void codigoDeDisciplinaEhUnico() {
		inserirDisciplina("Matemática", "MAT-1");

		assertThatThrownBy(() -> inserirDisciplina("Matemática avançada", "MAT-1"))
				.isInstanceOf(DuplicateKeyException.class)
				.hasMessageContaining("uk_disciplina_codigo");
	}

	@Test
	void nomesDeAutorEDeDisciplinaPodemSeRepetir() {
		inserirAutor("Machado de Assis");
		inserirDisciplina("Matemática", "MAT-1");

		assertThatCode(() -> inserirAutor("Machado de Assis")).doesNotThrowAnyException();
		assertThatCode(() -> inserirDisciplina("Matemática", "MAT-2")).doesNotThrowAnyException();
	}

	@Test
	void livroPodeFicarSemCategoria() {
		long autorId = inserirAutor("Autor");

		assertThatCode(() -> inserirLivro("0306406152", autorId, null)).doesNotThrowAnyException();
	}

	@Test
	void livroExigeAutorExistente() {
		assertThatThrownBy(() -> inserirLivro("0306406152", -1, null))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("fk_livro_autor");
	}

	@Test
	void naoExcluiAutorComLivros() {
		long autorId = inserirAutor("Autor");
		inserirLivro("0306406152", autorId, null);

		assertThatThrownBy(() -> jdbc.update("DELETE FROM autor WHERE id = ?", autorId))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("fk_livro_autor");
	}

	@Test
	void naoExcluiCategoriaComLivros() {
		long categoriaId = inserirCategoria("Literatura");
		inserirLivro("0306406152", inserirAutor("Autor"), categoriaId);

		assertThatThrownBy(() -> jdbc.update("DELETE FROM categoria WHERE id = ?", categoriaId))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("fk_livro_categoria");
	}

	@Test
	void naoExcluiDisciplinaComMateriais() {
		long disciplinaId = inserirDisciplina("Matemática", "MAT-1");
		jdbc.update("""
				INSERT INTO material_digital (titulo, formato, url, disciplina_id)
				VALUES ('Lista de exercícios', 'PDF', 'https://exemplo.com/lista.pdf', ?)
				""", disciplinaId);

		assertThatThrownBy(() -> jdbc.update("DELETE FROM disciplina WHERE id = ?", disciplinaId))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("fk_material_digital_disciplina");
	}

	private long inserirAutor(String nome) {
		return jdbc.queryForObject("INSERT INTO autor (nome) VALUES (?) RETURNING id", Long.class, nome);
	}

	private long inserirCategoria(String nome) {
		return jdbc.queryForObject(
				"INSERT INTO categoria (nome, tipo) VALUES (?, 'LITERATURA') RETURNING id", Long.class, nome);
	}

	private long inserirLivro(String isbn, long autorId, Long categoriaId) {
		return jdbc.queryForObject("""
				INSERT INTO livro (titulo, isbn, autor_id, categoria_id)
				VALUES ('Livro de teste', ?, ?, ?) RETURNING id
				""", Long.class, isbn, autorId, categoriaId);
	}

	private long inserirDisciplina(String nome, String codigo) {
		return jdbc.queryForObject("""
				INSERT INTO disciplina (nome, codigo, serie_escolar)
				VALUES (?, ?, 'PRIMEIRA_SERIE') RETURNING id
				""", Long.class, nome, codigo);
	}

}
