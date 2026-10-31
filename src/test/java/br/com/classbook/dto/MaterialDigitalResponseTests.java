package br.com.classbook.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.MaterialDigitalResponse.DisciplinaResumo;
import br.com.classbook.model.Disciplina;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.MaterialDigital;
import br.com.classbook.model.SerieEscolar;

/**
 * Confere que {@link MaterialDigitalResponse#de(MaterialDigital)} copia todos os campos da entidade,
 * inclusive o resumo da disciplina com o código. A transação é desfeita ao final.
 */
@SpringBootTest
@Transactional
class MaterialDigitalResponseTests {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void deCopiaTodosOsCamposDaEntidadeEOResumoDaDisciplina() {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Matemática");
		disciplina.setCodigo("MAT-1");
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);
		entityManager.persist(disciplina);

		MaterialDigital material = new MaterialDigital();
		material.setTitulo("Lista de funções");
		material.setFormato(FormatoMaterial.PDF);
		material.setUrl("https://exemplo.com/funcoes.pdf");
		material.setDescricao("Texto opcional");
		material.setDataPublicacao(LocalDate.of(2026, 3, 10));
		material.setDisciplina(disciplina);
		entityManager.persist(material);

		MaterialDigitalResponse response = MaterialDigitalResponse.de(material);

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new MaterialDigitalResponse(material.getId(), "Lista de funções",
				FormatoMaterial.PDF, "https://exemplo.com/funcoes.pdf", "Texto opcional", LocalDate.of(2026, 3, 10),
				new DisciplinaResumo(disciplina.getId(), "Matemática", "MAT-1")));
	}

}
