package br.com.classbook.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Categoria;
import br.com.classbook.model.TipoCategoria;

/**
 * Confere que {@link CategoriaRepository} salva e busca uma categoria no PostgreSQL. A transação é
 * desfeita ao final.
 */
@SpringBootTest
@Transactional
class CategoriaRepositoryTests {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Test
	void salvaEBuscaUmaCategoria() {
		Categoria categoria = new Categoria();
		categoria.setNome("Literatura");
		categoria.setTipo(TipoCategoria.LITERATURA);

		Categoria salva = categoriaRepository.save(categoria);

		Optional<Categoria> encontrada = categoriaRepository.findById(salva.getId());
		assertThat(encontrada).isPresent();
		assertThat(encontrada.get().getNome()).isEqualTo("Literatura");
		assertThat(encontrada.get().getTipo()).isEqualTo(TipoCategoria.LITERATURA);
	}

}
