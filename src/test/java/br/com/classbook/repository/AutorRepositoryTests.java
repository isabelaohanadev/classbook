package br.com.classbook.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Autor;

/**
 * Confere que {@link AutorRepository} salva e busca um autor no PostgreSQL. A transação é desfeita ao
 * final.
 */
@SpringBootTest
@Transactional
class AutorRepositoryTests {

	@Autowired
	private AutorRepository autorRepository;

	@Test
	void salvaEBuscaUmAutor() {
		Autor autor = new Autor();
		autor.setNome("Clarice Lispector");
		autor.setNacionalidade("Brasileira");

		Autor salvo = autorRepository.save(autor);

		Optional<Autor> encontrado = autorRepository.findById(salvo.getId());
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getNome()).isEqualTo("Clarice Lispector");
		assertThat(encontrado.get().getNacionalidade()).isEqualTo("Brasileira");
	}

}
