package br.com.classbook.repository;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Autor;
import br.com.classbook.model.Disciplina;
import br.com.classbook.model.Livro;
import br.com.classbook.model.SerieEscolar;

/**
 * Confere que {@link LivroRepository} salva, busca e consulta por ISBN um livro no PostgreSQL. A
 * transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class LivroRepositoryTests {

	@Autowired
	private LivroRepository livroRepository;

	@Autowired
	private AutorRepository autorRepository;

	@Autowired
	private DisciplinaRepository disciplinaRepository;

	@Test
	void salvaEBuscaUmLivro() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		autorRepository.save(autor);

		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAutor(autor);

		Livro salvo = livroRepository.save(livro);

		Optional<Livro> encontrado = livroRepository.findById(salvo.getId());
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getTitulo()).isEqualTo("Dom Casmurro");
	}

	@Test
	void existsByIsbnDistingueIsbnCadastradoDeNaoCadastrado() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		autorRepository.save(autor);

		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAutor(autor);
		livroRepository.save(livro);

		assertThat(livroRepository.existsByIsbn("9780306406157")).isTrue();
		assertThat(livroRepository.existsByIsbn("0000000000000")).isFalse();
	}

	@Test
	void existsByDisciplinaIdDistingueDisciplinaComLivroDeDisciplinaSemLivro() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		autorRepository.save(autor);

		Disciplina comLivro = disciplina("LP-1");
		Disciplina semLivro = disciplina("LP-2");

		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAutor(autor);
		livro.setDisciplina(comLivro);
		livroRepository.save(livro);

		assertThat(livroRepository.existsByDisciplinaId(comLivro.getId())).isTrue();
		assertThat(livroRepository.existsByDisciplinaId(semLivro.getId())).isFalse();
		assertThat(livroRepository.existsByDisciplinaId(-1L)).isFalse();
	}

	private Disciplina disciplina(String codigo) {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Língua Portuguesa");
		disciplina.setCodigo(codigo(codigo));
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);
		return disciplinaRepository.save(disciplina);
	}

}
