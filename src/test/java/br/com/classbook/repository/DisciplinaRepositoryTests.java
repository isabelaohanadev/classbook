package br.com.classbook.repository;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Disciplina;
import br.com.classbook.model.SerieEscolar;

/**
 * Confere que {@link DisciplinaRepository} salva e busca uma disciplina no PostgreSQL. A transação é
 * desfeita ao final.
 */
@SpringBootTest
@Transactional
class DisciplinaRepositoryTests {

	@Autowired
	private DisciplinaRepository disciplinaRepository;

	@Test
	void salvaEBuscaUmaDisciplina() {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Matemática");
		disciplina.setCodigo(codigo("MAT-1"));
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);

		Disciplina salva = disciplinaRepository.save(disciplina);

		Optional<Disciplina> encontrada = disciplinaRepository.findById(salva.getId());
		assertThat(encontrada).isPresent();
		assertThat(encontrada.get().getCodigo()).isEqualTo(codigo("MAT-1"));
		assertThat(encontrada.get().getSerieEscolar()).isEqualTo(SerieEscolar.PRIMEIRA_SERIE);
	}

}
