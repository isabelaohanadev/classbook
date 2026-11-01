package br.com.classbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.DisciplinaResponse;
import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.SerieEscolar;
import br.com.classbook.repository.DisciplinaRepository;

/**
 * Confere as regras de negócio de {@link DisciplinaService}. A transação é desfeita ao final de cada
 * teste.
 */
@SpringBootTest
@Transactional
class DisciplinaServiceTests {

	@Autowired
	private DisciplinaService disciplinaService;

	@Autowired
	private DisciplinaRepository disciplinaRepository;

	@Autowired
	private MaterialDigitalService materialDigitalService;

	@Test
	void criarGravaERetornaADisciplinaComOCodigoNormalizado() {
		DisciplinaResponse response = disciplinaService.criar(new DisciplinaRequest("Matemática", " mat-1 ",
				"Matemática da 1ª série", SerieEscolar.PRIMEIRA_SERIE));

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new DisciplinaResponse(response.id(), "Matemática", "MAT-1",
				"Matemática da 1ª série", SerieEscolar.PRIMEIRA_SERIE));
		assertThat(disciplinaRepository.findById(response.id())).isPresent();
	}

	@Test
	void listarRetornaOrdenadasPorCodigo() {
		disciplinaService.criar(new DisciplinaRequest("Química", "QUI-2", null, SerieEscolar.SEGUNDA_SERIE));
		disciplinaService.criar(new DisciplinaRequest("Física", "FIS-1", null, SerieEscolar.PRIMEIRA_SERIE));
		disciplinaService.criar(new DisciplinaRequest("Matemática", "MAT-3", null, SerieEscolar.TERCEIRA_SERIE));

		// Confere a ordem relativa, sem supor que o banco de teste começa vazio.
		assertThat(disciplinaService.listar()).extracting(DisciplinaResponse::codigo)
				.containsSubsequence("FIS-1", "MAT-3", "QUI-2");
	}

	@Test
	void buscarPorIdRetornaADisciplinaExistente() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		assertThat(disciplinaService.buscarPorId(criada.id())).isEqualTo(criada);
	}

	@Test
	void buscarPorIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> disciplinaService.buscarPorId(-1L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarSubstituiOsCamposDaDisciplina() {
		DisciplinaResponse criada = disciplinaService.criar(new DisciplinaRequest("Nome Antigo", "ANT-1",
				"Descrição antiga", SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizar(criada.id(),
				new DisciplinaRequest("Nome Novo", "nov-2", null, SerieEscolar.SEGUNDA_SERIE));

		assertThat(atualizada).isEqualTo(new DisciplinaResponse(criada.id(), "Nome Novo", "NOV-2", null,
				SerieEscolar.SEGUNDA_SERIE));
	}

	@Test
	void atualizarIdInexistenteLancaExcecao() {
		DisciplinaRequest request = new DisciplinaRequest("Nome", "COD-1", null, SerieEscolar.PRIMEIRA_SERIE);

		assertThatThrownBy(() -> disciplinaService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirRemoveADisciplina() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Descartável", "DESC-1", null, SerieEscolar.PRIMEIRA_SERIE));

		disciplinaService.excluir(criada.id());

		assertThat(disciplinaRepository.findById(criada.id())).isEmpty();
	}

	@Test
	void excluirIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> disciplinaService.excluir(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirDisciplinaComMateriaisVinculadosLancaConflito() {
		DisciplinaResponse disciplina = disciplinaService
				.criar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));
		materialDigitalService.criar(new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF,
				"https://exemplo.com/funcoes.pdf", null, null, disciplina.id()));

		assertThatThrownBy(() -> disciplinaService.excluir(disciplina.id())).isInstanceOf(ConflitoException.class)
				.hasMessage("Disciplina " + disciplina.id() + " possui materiais vinculados.");
		assertThat(disciplinaRepository.findById(disciplina.id())).isPresent();
	}

	@Test
	void criarComCodigoJaUsadoLancaConflitoDepoisDaNormalizacao() {
		disciplinaService.criar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));
		DisciplinaRequest duplicada = new DisciplinaRequest("Outra", " mat-1 ", null, SerieEscolar.SEGUNDA_SERIE);

		assertThatThrownBy(() -> disciplinaService.criar(duplicada)).isInstanceOf(ConflitoException.class);
	}

	@Test
	void mesmoNomeComCodigosDiferentesNaoEhConflito() {
		disciplinaService.criar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse outraSerie = disciplinaService
				.criar(new DisciplinaRequest("Matemática", "MAT-2", null, SerieEscolar.SEGUNDA_SERIE));

		assertThat(outraSerie.id()).isNotNull();
	}

	@Test
	void atualizarComCodigoDeOutraDisciplinaLancaConflito() {
		disciplinaService.criar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));
		DisciplinaResponse outra = disciplinaService
				.criar(new DisciplinaRequest("Física", "FIS-1", null, SerieEscolar.PRIMEIRA_SERIE));
		DisciplinaRequest edicao = new DisciplinaRequest("Física", "mat-1", null, SerieEscolar.PRIMEIRA_SERIE);

		assertThatThrownBy(() -> disciplinaService.atualizar(outra.id(), edicao))
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void atualizarMantendoOProprioCodigoNaoEhConflito() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizar(criada.id(),
				new DisciplinaRequest("Matemática Básica", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE));

		assertThat(atualizada.nome()).isEqualTo("Matemática Básica");
	}

}
