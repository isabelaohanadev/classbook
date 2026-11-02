package br.com.classbook.service;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.dto.MaterialDigitalResponse;
import br.com.classbook.dto.MaterialDigitalResponse.DisciplinaResumo;
import br.com.classbook.exception.DadosInvalidosException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.SerieEscolar;
import br.com.classbook.repository.MaterialDigitalRepository;

/**
 * Confere as regras de negócio de {@link MaterialDigitalService}. A transação é desfeita ao final de
 * cada teste.
 */
@SpringBootTest
@Transactional
class MaterialDigitalServiceTests {

	@Autowired
	private MaterialDigitalService materialDigitalService;

	@Autowired
	private DisciplinaService disciplinaService;

	@Autowired
	private MaterialDigitalRepository materialDigitalRepository;

	private Long disciplinaId;

	@BeforeEach
	void criarDisciplina() {
		disciplinaId = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE)).id();
	}

	@Test
	void criarGravaERetornaOMaterialComADisciplinaResumida() {
		MaterialDigitalRequest request = new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF,
				"https://exemplo.com/funcoes.pdf", "Texto", LocalDate.of(2026, 3, 10), disciplinaId);

		MaterialDigitalResponse response = materialDigitalService.criar(request);

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new MaterialDigitalResponse(response.id(), "Lista de funções",
				FormatoMaterial.PDF, "https://exemplo.com/funcoes.pdf", "Texto", LocalDate.of(2026, 3, 10),
				new DisciplinaResumo(disciplinaId, "Matemática", codigo("MAT-1"))));
		assertThat(materialDigitalRepository.findById(response.id())).isPresent();
	}

	@Test
	void criarComDisciplinaInexistenteLancaDadosInvalidos() {
		MaterialDigitalRequest request = material("Lista de funções", -1L);

		assertThatThrownBy(() -> materialDigitalService.criar(request))
				.isInstanceOf(DadosInvalidosException.class)
				.hasMessage("Disciplina -1 não existe.");
	}

	@Test
	void listarRetornaOrdenadosPorTitulo() {
		materialDigitalService.criar(material("Videoaula de funções", disciplinaId));
		materialDigitalService.criar(material("Apostila de funções", disciplinaId));
		materialDigitalService.criar(material("Lista de funções", disciplinaId));

		// Confere a ordem relativa, sem supor que o banco de teste começa vazio.
		assertThat(materialDigitalService.listar()).extracting(MaterialDigitalResponse::titulo)
				.containsSubsequence("Apostila de funções", "Lista de funções", "Videoaula de funções");
	}

	@Test
	void buscarPorIdRetornaOMaterialExistente() {
		MaterialDigitalResponse criado = materialDigitalService.criar(material("Lista de funções", disciplinaId));

		assertThat(materialDigitalService.buscarPorId(criado.id())).isEqualTo(criado);
	}

	@Test
	void buscarPorIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> materialDigitalService.buscarPorId(-1L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarSubstituiOsCamposEPodeTrocarADisciplina() {
		MaterialDigitalResponse criado = materialDigitalService.criar(new MaterialDigitalRequest("Título antigo",
				FormatoMaterial.PDF, "https://exemplo.com/antigo.pdf", "Texto", LocalDate.of(2026, 3, 10),
				disciplinaId));
		Long outraDisciplinaId = disciplinaService
				.criar(new DisciplinaRequest("Física", codigo("FIS-1"), null, SerieEscolar.PRIMEIRA_SERIE)).id();

		MaterialDigitalResponse atualizado = materialDigitalService.atualizar(criado.id(), new MaterialDigitalRequest(
				"Título novo", FormatoMaterial.VIDEO, "https://exemplo.com/novo", null, null, outraDisciplinaId));

		assertThat(atualizado).isEqualTo(new MaterialDigitalResponse(criado.id(), "Título novo", FormatoMaterial.VIDEO,
				"https://exemplo.com/novo", null, null,
				new DisciplinaResumo(outraDisciplinaId, "Física", codigo("FIS-1"))));
	}

	@Test
	void atualizarIdInexistenteLancaExcecao() {
		MaterialDigitalRequest request = material("Título", disciplinaId);

		assertThatThrownBy(() -> materialDigitalService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarComDisciplinaInexistenteLancaDadosInvalidos() {
		MaterialDigitalResponse criado = materialDigitalService.criar(material("Título", disciplinaId));
		MaterialDigitalRequest edicao = material("Título", -1L);

		assertThatThrownBy(() -> materialDigitalService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void excluirRemoveOMaterial() {
		MaterialDigitalResponse criado = materialDigitalService.criar(material("Descartável", disciplinaId));

		materialDigitalService.excluir(criado.id());

		assertThat(materialDigitalRepository.findById(criado.id())).isEmpty();
	}

	@Test
	void excluirIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> materialDigitalService.excluir(-1L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	private static MaterialDigitalRequest material(String titulo, Long disciplinaId) {
		return new MaterialDigitalRequest(titulo, FormatoMaterial.PDF, "https://exemplo.com/material.pdf", null,
				null, disciplinaId);
	}

}
