package br.com.classbook.service;

import static br.com.classbook.Unico.codigo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolationException;

import tools.jackson.core.JacksonException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.Json;
import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.DisciplinaResponse;
import br.com.classbook.dto.LivroRequest;
import br.com.classbook.dto.LivroResponse;
import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Autor;
import br.com.classbook.model.FormatoMaterial;
import br.com.classbook.model.SerieEscolar;
import br.com.classbook.repository.AutorRepository;
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

	@Autowired
	private LivroService livroService;

	@Autowired
	private AutorRepository autorRepository;

	@Test
	void criarGravaERetornaADisciplinaComOCodigoNormalizado() {
		DisciplinaResponse response = disciplinaService.criar(new DisciplinaRequest("Matemática",
				" " + codigo("mat-1") + " ",
				"Matemática da 1ª série", SerieEscolar.PRIMEIRA_SERIE));

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new DisciplinaResponse(response.id(), "Matemática", codigo("MAT-1"),
				"Matemática da 1ª série", SerieEscolar.PRIMEIRA_SERIE));
		assertThat(disciplinaRepository.findById(response.id())).isPresent();
	}

	@Test
	void listarRetornaOrdenadasPorCodigo() {
		disciplinaService.criar(new DisciplinaRequest("Química", codigo("QUI-2"), null, SerieEscolar.SEGUNDA_SERIE));
		disciplinaService.criar(new DisciplinaRequest("Física", codigo("FIS-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		disciplinaService.criar(new DisciplinaRequest("Matemática", codigo("MAT-3"), null,
				SerieEscolar.TERCEIRA_SERIE));

		// Confere a ordem relativa, sem supor que o banco de teste começa vazio.
		assertThat(disciplinaService.listar()).extracting(DisciplinaResponse::codigo)
				.containsSubsequence(codigo("FIS-1"), codigo("MAT-3"), codigo("QUI-2"));
	}

	@Test
	void buscarPorIdRetornaADisciplinaExistente() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		assertThat(disciplinaService.buscarPorId(criada.id())).isEqualTo(criada);
	}

	@Test
	void buscarPorIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> disciplinaService.buscarPorId(-1L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarSubstituiOsCamposDaDisciplina() {
		DisciplinaResponse criada = disciplinaService.criar(new DisciplinaRequest("Nome Antigo", codigo("ANT-1"),
				"Descrição antiga", SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizar(criada.id(),
				new DisciplinaRequest("Nome Novo", codigo("nov-2"), null, SerieEscolar.SEGUNDA_SERIE));

		assertThat(atualizada).isEqualTo(new DisciplinaResponse(criada.id(), "Nome Novo", codigo("NOV-2"), null,
				SerieEscolar.SEGUNDA_SERIE));
	}

	@Test
	void atualizarIdInexistenteLancaExcecao() {
		DisciplinaRequest request = new DisciplinaRequest("Nome", codigo("COD-1"), null, SerieEscolar.PRIMEIRA_SERIE);

		assertThatThrownBy(() -> disciplinaService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirRemoveADisciplina() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Descartável", codigo("DESC-1"), null, SerieEscolar.PRIMEIRA_SERIE));

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
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		materialDigitalService.criar(new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF,
				"https://exemplo.com/funcoes.pdf", null, null, disciplina.id()));

		assertThatThrownBy(() -> disciplinaService.excluir(disciplina.id())).isInstanceOf(ConflitoException.class)
				.hasMessage("Disciplina " + disciplina.id() + " possui materiais vinculados.");
		assertThat(disciplinaRepository.findById(disciplina.id())).isPresent();
	}

	@Test
	void excluirDisciplinaComLivrosVinculadosLancaConflito() {
		DisciplinaResponse disciplina = disciplinaService
				.criar(new DisciplinaRequest("Língua Portuguesa", codigo("LP-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		Long autorId = autorRepository.save(autor).getId();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null,
				disciplina.id()));

		assertThatThrownBy(() -> disciplinaService.excluir(disciplina.id())).isInstanceOf(ConflitoException.class)
				.hasMessage("Disciplina " + disciplina.id() + " possui livros vinculados.");
		assertThat(disciplinaRepository.findById(disciplina.id())).isPresent();
	}

	@Test
	void excluirDisciplinaDepoisDeTirarOLivroDelaFunciona() {
		DisciplinaResponse disciplina = disciplinaService
				.criar(new DisciplinaRequest("Língua Portuguesa", codigo("LP-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		Long autorId = autorRepository.save(autor).getId();
		LivroResponse livro = livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId,
				null, disciplina.id()));
		livroService.atualizar(livro.id(),
				new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		disciplinaService.excluir(disciplina.id());

		assertThat(disciplinaRepository.findById(disciplina.id())).isEmpty();
	}

	@Test
	void criarComCodigoJaUsadoLancaConflitoDepoisDaNormalizacao() {
		disciplinaService.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null,
				SerieEscolar.PRIMEIRA_SERIE));
		DisciplinaRequest duplicada = new DisciplinaRequest("Outra", " " + codigo("mat-1") + " ", null,
				SerieEscolar.SEGUNDA_SERIE);

		assertThatThrownBy(() -> disciplinaService.criar(duplicada)).isInstanceOf(ConflitoException.class);
	}

	@Test
	void mesmoNomeComCodigosDiferentesNaoEhConflito() {
		disciplinaService.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null,
				SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse outraSerie = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-2"), null, SerieEscolar.SEGUNDA_SERIE));

		assertThat(outraSerie.id()).isNotNull();
	}

	@Test
	void atualizarComCodigoDeOutraDisciplinaLancaConflito() {
		disciplinaService.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null,
				SerieEscolar.PRIMEIRA_SERIE));
		DisciplinaResponse outra = disciplinaService
				.criar(new DisciplinaRequest("Física", codigo("FIS-1"), null, SerieEscolar.PRIMEIRA_SERIE));
		DisciplinaRequest edicao = new DisciplinaRequest("Física", codigo("mat-1"), null, SerieEscolar.PRIMEIRA_SERIE);

		assertThatThrownBy(() -> disciplinaService.atualizar(outra.id(), edicao))
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void atualizarMantendoOProprioCodigoNaoEhConflito() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizar(criada.id(),
				new DisciplinaRequest("Matemática Básica", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		assertThat(atualizada.nome()).isEqualTo("Matemática Básica");
	}

	@Test
	void atualizarParcialmenteMudaSoOsCamposEnviados() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), "1ª série", SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizarParcialmente(criada.id(),
				Json.de("{'descricao': 'Matemática da 1ª série do ensino médio'}"));

		assertThat(atualizada).isEqualTo(new DisciplinaResponse(criada.id(), "Matemática", codigo("MAT-1"),
				"Matemática da 1ª série do ensino médio", SerieEscolar.PRIMEIRA_SERIE));
		assertThat(disciplinaRepository.findById(criada.id()).orElseThrow().getDescricao())
				.isEqualTo("Matemática da 1ª série do ensino médio");
	}

	@Test
	void atualizarParcialmenteComNullLimpaOCampoOpcional() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), "1ª série", SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizarParcialmente(criada.id(),
				Json.de("{'descricao': null}"));

		assertThat(atualizada.descricao()).isNull();
		assertThat(atualizada.codigo()).isEqualTo(codigo("MAT-1"));
	}

	@Test
	void atualizarParcialmenteNormalizaOCodigo() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizarParcialmente(criada.id(),
				Json.de("{'codigo': ' " + codigo("mat-2") + " '}"));

		assertThat(atualizada.codigo()).isEqualTo(codigo("MAT-2"));
	}

	@Test
	void atualizarParcialmenteComCodigoDeOutraDisciplinaLancaConflito() {
		disciplinaService.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null,
				SerieEscolar.PRIMEIRA_SERIE));
		DisciplinaResponse outra = disciplinaService
				.criar(new DisciplinaRequest("Física", codigo("FIS-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		assertThatThrownBy(() -> disciplinaService.atualizarParcialmente(outra.id(),
				Json.de("{'codigo': '" + codigo("mat-1") + "'}"))).isInstanceOf(ConflitoException.class);
	}

	@Test
	void atualizarParcialmenteMantendoOProprioCodigoNaoEhConflito() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		DisciplinaResponse atualizada = disciplinaService.atualizarParcialmente(criada.id(),
				Json.de("{'codigo': '" + codigo("mat-1") + "', 'nome': 'Matemática Básica'}"));

		assertThat(atualizada.nome()).isEqualTo("Matemática Básica");
	}

	@Test
	void atualizarParcialmenteComSerieNullLancaViolacaoDeValidacao() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		assertThatThrownBy(() -> disciplinaService.atualizarParcialmente(criada.id(),
				Json.de("{'serieEscolar': null}")))
				.isInstanceOfSatisfying(ConstraintViolationException.class, ex -> assertThat(ex.getConstraintViolations())
						.extracting(violacao -> violacao.getPropertyPath().toString()).containsExactly("serieEscolar"));
	}

	@Test
	void atualizarParcialmenteComSerieForaDaListaLancaErroDoJackson() {
		DisciplinaResponse criada = disciplinaService
				.criar(new DisciplinaRequest("Matemática", codigo("MAT-1"), null, SerieEscolar.PRIMEIRA_SERIE));

		assertThatThrownBy(() -> disciplinaService.atualizarParcialmente(criada.id(),
				Json.de("{'serieEscolar': 'QUARTA_SERIE'}"))).isInstanceOf(JacksonException.class);
	}

	@Test
	void atualizarParcialmenteComIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> disciplinaService.atualizarParcialmente(-1L, Json.de("{'nome': 'Novo'}")))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

}
