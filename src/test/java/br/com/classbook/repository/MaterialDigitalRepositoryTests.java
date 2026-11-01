package br.com.classbook.repository;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.model.Disciplina;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.MaterialDigital;
import br.com.classbook.model.SerieEscolar;

/**
 * Confere que {@link MaterialDigitalRepository} salva e busca um material no PostgreSQL, e que a
 * listagem já traz a disciplina carregada. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class MaterialDigitalRepositoryTests {

	@Autowired
	private MaterialDigitalRepository materialDigitalRepository;

	@Autowired
	private DisciplinaRepository disciplinaRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void salvaEBuscaUmMaterial() {
		MaterialDigital salvo = materialDigitalRepository.save(novoMaterial("Lista de funções", novaDisciplina()));

		assertThat(materialDigitalRepository.findById(salvo.getId()))
				.hasValueSatisfying(material -> assertThat(material.getTitulo()).isEqualTo("Lista de funções"));
	}

	@Test
	void listagemCarregaADisciplinaNaMesmaConsulta() {
		MaterialDigital salvo = materialDigitalRepository.save(novoMaterial("Lista de funções", novaDisciplina()));
		entityManager.flush();
		entityManager.clear();

		List<MaterialDigital> materiais = materialDigitalRepository.findAll(Sort.by("titulo"));

		MaterialDigital lido = materiais.stream().filter(m -> m.getId().equals(salvo.getId())).findFirst()
				.orElseThrow();
		assertThat(Hibernate.isInitialized(lido.getDisciplina())).isTrue();
	}

	private Disciplina novaDisciplina() {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Matemática");
		disciplina.setCodigo(codigo("MAT-1"));
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);
		return disciplinaRepository.save(disciplina);
	}

	private static MaterialDigital novoMaterial(String titulo, Disciplina disciplina) {
		MaterialDigital material = new MaterialDigital();
		material.setTitulo(titulo);
		material.setFormato(FormatoMaterial.PDF);
		material.setUrl("https://exemplo.com/funcoes.pdf");
		material.setDisciplina(disciplina);
		return material;
	}

}
