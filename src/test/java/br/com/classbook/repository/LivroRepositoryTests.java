package br.com.classbook.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Autor;
import br.com.classbook.model.Livro;

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

}
