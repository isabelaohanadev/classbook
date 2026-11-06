package br.com.classbook.service;

import static br.com.classbook.Unico.codigo;
import static br.com.classbook.Unico.nome;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.LivroRequest;
import br.com.classbook.dto.LivroResponse;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.DadosInvalidosException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Autor;
import br.com.classbook.model.Categoria;
import br.com.classbook.model.Disciplina;
import br.com.classbook.model.SerieEscolar;
import br.com.classbook.model.TipoCategoria;
import br.com.classbook.repository.AutorRepository;
import br.com.classbook.repository.CategoriaRepository;
import br.com.classbook.repository.DisciplinaRepository;
import br.com.classbook.repository.LivroRepository;

/**
 * Confere as regras de negócio de {@link LivroService}. A transação é desfeita ao final de cada
 * teste.
 */
@SpringBootTest
@Transactional
class LivroServiceTests {

	@Autowired
	private LivroService livroService;

	@Autowired
	private LivroRepository livroRepository;

	@Autowired
	private AutorRepository autorRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private DisciplinaRepository disciplinaRepository;

	private Long autorId;

	private Long criarAutor() {
		return autorRepository.save(autorEntidade("Machado de Assis")).getId();
	}

	private Autor autorEntidade(String nome) {
		Autor autor = new Autor();
		autor.setNome(nome);
		return autor;
	}

	private Long criarCategoria(String nome) {
		Categoria categoria = new Categoria();
		categoria.setNome(nome);
		categoria.setTipo(TipoCategoria.LITERATURA);
		return categoriaRepository.save(categoria).getId();
	}

	private Long criarDisciplina(String codigo) {
		Disciplina disciplina = new Disciplina();
		disciplina.setNome("Língua Portuguesa");
		disciplina.setCodigo(codigo);
		disciplina.setSerieEscolar(SerieEscolar.PRIMEIRA_SERIE);
		return disciplinaRepository.save(disciplina).getId();
	}

	@Test
	void criarGravaERetornaOLivroComOAutor() {
		autorId = criarAutor();

		LivroResponse response = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId, null, null));

		assertThat(response.id()).isNotNull();
		assertThat(response.titulo()).isEqualTo("Dom Casmurro");
		assertThat(response.autor().id()).isEqualTo(autorId);
		assertThat(livroRepository.findById(response.id())).isPresent();
	}

	@Test
	void criarComAnoDePublicacaoNoFuturoLancaDadosInvalidos() {
		autorId = criarAutor();
		int anoQueVem = Year.now().getValue() + 1;
		LivroRequest request = new LivroRequest("Livro do Futuro", "9780306406157", anoQueVem, null, null, autorId, null, null);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class)
				.hasMessage("O ano de publicação " + anoQueVem + " está no futuro.");
	}

	@Test
	void anoDePublicacaoAtualEhAceito() {
		autorId = criarAutor();
		int anoAtual = Year.now().getValue();

		LivroResponse response = livroService
				.criar(new LivroRequest("Lançamento", "9780306406157", anoAtual, null, null, autorId, null, null));

		assertThat(response.anoPublicacao()).isEqualTo(anoAtual);
	}

	@Test
	void atualizarComAnoDePublicacaoNoFuturoLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", Year.now().getValue() + 1, null, null,
				autorId, null, null);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void criarComAutorInexistenteLancaDadosInvalidos() {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, -1L, null, null);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void criarComIsbnJaCadastradoLancaConflito() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest duplicado = new LivroRequest("Outro Título", "9780306406157", null, null, null, autorId, null, null);

		assertThatThrownBy(() -> livroService.criar(duplicado)).isInstanceOf(ConflitoException.class)
				.hasMessage("Já existe um livro com o ISBN 9780306406157.");
	}

	@Test
	void criarComIsbnJaCadastradoComHifensDiferentesLancaConflito() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "978-0-306-40615-7", null, null, null, autorId, null, null));
		LivroRequest duplicado = new LivroRequest("Outro Título", "9780306406157", null, null, null, autorId, null, null);

		assertThatThrownBy(() -> livroService.criar(duplicado)).isInstanceOf(ConflitoException.class);
	}

	@Test
	void listarRetornaTodosOrdenadosPorTitulo() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId, null, null));

		List<LivroResponse> livros = livroService.listar(null, null, null, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro", "Memórias Póstumas");
	}

	@Test
	void listarComTituloFiltraSemDiferenciarMaiusculas() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId, null, null));

		List<LivroResponse> livros = livroService.listar("casmurro", null, null, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void listarComTituloSemCorrespondenciaRetornaListaVazia() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		assertThat(livroService.listar("inexistente", null, null, null)).isEmpty();
	}

	@Test
	void listarComCuringaDoLikeNoTituloProcuraOCaractereComoTexto() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("100% Jovem", "9780316000000", null, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Guia_de_Estudos", "8535910654", null, null, null, autorId, null, null));

		// O autorId restringe a pesquisa aos livros deste teste, qualquer que seja o conteúdo do banco.
		assertThat(livroService.listar("%", null, autorId, null)).extracting(LivroResponse::titulo)
				.containsExactly("100% Jovem");
		assertThat(livroService.listar("_", null, autorId, null)).extracting(LivroResponse::titulo)
				.containsExactly("Guia_de_Estudos");
		assertThat(livroService.listar("\\", null, autorId, null)).isEmpty();
	}

	@Test
	void listarComAutorIdFiltraSomenteOsLivrosDesseAutor() {
		Long machado = criarAutor();
		Long outroAutor = autorRepository.save(autorEntidade("José de Alencar")).getId();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, machado, null, null));
		livroService.criar(new LivroRequest("Senhora", "9780316000000", null, null, null, outroAutor, null, null));

		List<LivroResponse> livros = livroService.listar(null, null, machado, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void listarComAutorIdInexistenteRetornaListaVazia() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		assertThat(livroService.listar(null, null, -1L, null)).isEmpty();
	}

	@Test
	void listarComCategoriaIdFiltraSomenteOsLivrosDessaCategoria() {
		autorId = criarAutor();
		Long literatura = criarCategoria(nome("Literatura"));
		Long referencia = criarCategoria(nome("Referência"));
		livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, literatura, null));
		livroService
				.criar(new LivroRequest("Dicionário", "9780316000000", null, null, null, autorId, referencia, null));

		List<LivroResponse> livros = livroService.listar(null, null, null, literatura);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void listarComAutorIdECategoriaIdCombinaOsFiltros() {
		Long machado = criarAutor();
		Long outroAutor = autorRepository.save(autorEntidade("José de Alencar")).getId();
		Long literatura = criarCategoria(nome("Literatura"));
		livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, machado, literatura, null));
		livroService
				.criar(new LivroRequest("Senhora", "9780316000000", null, null, null, outroAutor, literatura, null));
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780321000000", null, null, null, machado, null, null));

		List<LivroResponse> livros = livroService.listar(null, null, machado, literatura);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void listarComTituloEAutorIdCombinaOsFiltrosSemCategoriaId() {
		Long machado = criarAutor();
		Long outroAutor = autorRepository.save(autorEntidade("José de Alencar")).getId();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, machado, null, null));
		livroService.criar(new LivroRequest("Dom Sebastião", "9780316000000", null, null, null, outroAutor, null, null));

		List<LivroResponse> livros = livroService.listar("dom", null, machado, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void listarComTituloECategoriaIdCombinaOsFiltrosSemAutorId() {
		autorId = criarAutor();
		Long literatura = criarCategoria(nome("Literatura"));
		Long referencia = criarCategoria(nome("Referência"));
		livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, literatura, null));
		livroService
				.criar(new LivroRequest("Dom Dicionário", "9780316000000", null, null, null, autorId, referencia, null));

		List<LivroResponse> livros = livroService.listar("dom", null, null, literatura);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void listarComTituloAutorIdECategoriaIdCombinadosSemCorrespondenciaRetornaListaVazia() {
		autorId = criarAutor();
		Long literatura = criarCategoria(nome("Literatura"));
		livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, literatura, null));

		assertThat(livroService.listar("Senhora", null, autorId, literatura)).isEmpty();
	}

	@Test
	void buscarPorIdRetornaOLivroExistente() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		assertThat(livroService.buscarPorId(criado.id())).isEqualTo(criado);
	}

	@Test
	void buscarPorIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> livroService.buscarPorId(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarSubstituiOsCamposDoLivro() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título Antigo", "9780306406157", null, null, null, autorId, null, null));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título Novo", "9780316000000", 2020, "Nova descrição", "Nova editora", autorId, null, null));

		assertThat(atualizado.titulo()).isEqualTo("Título Novo");
		assertThat(atualizado.isbn()).isEqualTo("9780316000000");
		assertThat(atualizado.anoPublicacao()).isEqualTo(2020);
	}

	@Test
	void atualizarComIdInexistenteLancaExcecao() {
		autorId = criarAutor();
		LivroRequest request = new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null);

		assertThatThrownBy(() -> livroService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarComAutorInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, -1L, null, null);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void atualizarComIsbnDeOutroLivroLancaConflito() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));
		LivroResponse outro = livroService
				.criar(new LivroRequest("Memórias Póstumas", "9780316000000", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null, null);

		assertThatThrownBy(() -> livroService.atualizar(outro.id(), edicao)).isInstanceOf(ConflitoException.class)
				.hasMessage("Já existe um livro com o ISBN 9780306406157.");
	}

	@Test
	void atualizarMantendoOProprioIsbnNaoEhConflito() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título Antigo", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Título Novo", "9780306406157", null, null, null, autorId, null, null);

		LivroResponse atualizado = livroService.atualizar(criado.id(), edicao);

		assertThat(atualizado.titulo()).isEqualTo("Título Novo");
	}

	@Test
	void excluirRemoveOLivro() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		livroService.excluir(criado.id());

		assertThat(livroRepository.findById(criado.id())).isEmpty();
	}

	@Test
	void excluirComIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> livroService.excluir(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void criarSemCategoriaRetornaOLivroComACategoriaNula() {
		autorId = criarAutor();

		LivroResponse response = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		assertThat(response.categoria()).isNull();
	}

	@Test
	void criarComCategoriaValidaRetornaOLivroComACategoria() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Literatura"));

		LivroResponse response = livroService.criar(
				new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, categoriaId, null));

		assertThat(response.categoria().id()).isEqualTo(categoriaId);
		assertThat(response.categoria().nome()).isEqualTo(nome("Literatura"));
	}

	@Test
	void criarComCategoriaInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, -1L, null);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void atualizarComCategoriaValidaAtualizaACategoriaDoLivro() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Literatura"));
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId, null));

		assertThat(atualizado.categoria().id()).isEqualTo(categoriaId);
	}

	@Test
	void atualizarSemCategoriaIdRemoveACategoriaDoLivro() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Literatura"));
		LivroResponse criado = livroService.criar(
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId, null));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		assertThat(atualizado.categoria()).isNull();
	}

	@Test
	void atualizarComCategoriaInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId, -1L, null);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void criarSemDisciplinaRetornaOLivroComADisciplinaNula() {
		autorId = criarAutor();

		LivroResponse response = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		assertThat(response.disciplina()).isNull();
	}

	@Test
	void criarComDisciplinaValidaRetornaOLivroComADisciplinaEOCodigo() {
		autorId = criarAutor();
		Long disciplinaId = criarDisciplina(codigo("LP-1"));

		LivroResponse response = livroService.criar(
				new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, disciplinaId));

		assertThat(response.disciplina().id()).isEqualTo(disciplinaId);
		assertThat(response.disciplina().nome()).isEqualTo("Língua Portuguesa");
		assertThat(response.disciplina().codigo()).isEqualTo(codigo("LP-1"));
		assertThat(livroRepository.findById(response.id()).orElseThrow().getDisciplina().getId())
				.isEqualTo(disciplinaId);
	}

	@Test
	void criarComCategoriaEDisciplinaMantemAsDuasLigacoes() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Literatura"));
		Long disciplinaId = criarDisciplina(codigo("LP-1"));

		LivroResponse response = livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null,
				null, autorId, categoriaId, disciplinaId));

		assertThat(response.categoria().id()).isEqualTo(categoriaId);
		assertThat(response.disciplina().id()).isEqualTo(disciplinaId);
	}

	@Test
	void criarComDisciplinaInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null,
				-1L);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class)
				.hasMessage("Disciplina -1 não existe.");
		assertThat(livroRepository.existsByIsbn("9780306406157")).isFalse();
	}

	@Test
	void atualizarComDisciplinaValidaAtualizaADisciplinaDoLivro() {
		autorId = criarAutor();
		Long disciplinaId = criarDisciplina(codigo("LP-1"));
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, disciplinaId));

		assertThat(atualizado.disciplina().id()).isEqualTo(disciplinaId);
	}

	@Test
	void atualizarTrocandoADisciplinaSubstituiAAnterior() {
		autorId = criarAutor();
		Long primeira = criarDisciplina(codigo("LP-1"));
		Long segunda = criarDisciplina(codigo("LP-2"));
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, primeira));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, segunda));

		assertThat(atualizado.disciplina().id()).isEqualTo(segunda);
	}

	@Test
	void atualizarSemDisciplinaIdRemoveADisciplinaDoLivro() {
		autorId = criarAutor();
		Long disciplinaId = criarDisciplina(codigo("LP-1"));
		LivroResponse criado = livroService.criar(
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, disciplinaId));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		assertThat(atualizado.disciplina()).isNull();
	}

	@Test
	void atualizarComDisciplinaInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, -1L);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void listarTrazADisciplinaDeCadaLivro() {
		autorId = criarAutor();
		Long disciplinaId = criarDisciplina(codigo("LP-1"));
		livroService.criar(
				new LivroRequest("Com disciplina", "9780306406157", null, null, null, autorId, null, disciplinaId));
		livroService.criar(new LivroRequest("Sem disciplina", "0306406152", null, null, null, autorId, null, null));

		List<LivroResponse> livros = livroService.listar(null, null, autorId, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Com disciplina", "Sem disciplina");
		assertThat(livros.get(0).disciplina().id()).isEqualTo(disciplinaId);
		assertThat(livros.get(1).disciplina()).isNull();
	}

	// ---- Busca livre (parâmetro busca): título, autor, categoria e ano ----

	@Test
	void buscaEncontraPeloTituloSemDiferenciarMaiusculas() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780306406157", 1881, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780316000000", 1899, null, null, autorId, null, null));

		// O autorId restringe a pesquisa aos livros deste teste, qualquer que seja o conteúdo do banco.
		List<LivroResponse> livros = livroService.listar(null, "CASMURRO", autorId, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void buscaEncontraPeloNomeDoAutorMesmoQueOTituloNaoTenhaOTexto() {
		Long autorZeta = autorRepository.save(autorEntidade(nome("Autora Zeta"))).getId();
		Long outroAutor = autorRepository.save(autorEntidade(nome("Autor Outro"))).getId();
		livroService.criar(new LivroRequest("Primeiro Livro", "9780306406157", null, null, null, autorZeta, null, null));
		livroService.criar(new LivroRequest("Segundo Livro", "9780316000000", null, null, null, autorZeta, null, null));
		livroService.criar(new LivroRequest("Livro de Outro", "9780321000000", null, null, null, outroAutor, null, null));

		// O sufixo de nome() é único por execução, então só os livros deste teste podem bater.
		List<LivroResponse> livros = livroService.listar(null, nome("autora zeta"), null, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Primeiro Livro", "Segundo Livro");
	}

	@Test
	void buscaEncontraPeloNomeDaCategoria() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Categoria Zeta"));
		livroService.criar(new LivroRequest("Com categoria", "9780306406157", null, null, null, autorId, categoriaId, null));
		livroService.criar(new LivroRequest("Sem categoria", "9780316000000", null, null, null, autorId, null, null));

		List<LivroResponse> livros = livroService.listar(null, nome("categoria zeta"), null, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Com categoria");
	}

	@Test
	void buscaEncontraPeloAnoDePublicacaoInteiroOuParcial() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780316000000", 1881, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("A Hora da Estrela", "9780321000000", 1977, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Sem ano", "9780000000001", null, null, null, autorId, null, null));

		assertThat(livroService.listar(null, "1899", autorId, null)).extracting(LivroResponse::titulo)
				.containsExactly("Dom Casmurro");
		assertThat(livroService.listar(null, "18", autorId, null)).extracting(LivroResponse::titulo)
				.containsExactly("Dom Casmurro", "Memórias Póstumas");
		assertThat(livroService.listar(null, "2050", autorId, null)).isEmpty();
	}

	@Test
	void buscaPeloTituloNaoEscondeOsLivrosSemCategoriaNemSemAno() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Livro Simples", "9780306406157", null, null, null, autorId, null, null));

		List<LivroResponse> livros = livroService.listar(null, "simples", autorId, null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Livro Simples");
		assertThat(livros.get(0).categoria()).isNull();
		assertThat(livros.get(0).anoPublicacao()).isNull();
	}

	@Test
	void buscaTrazOLivroUmaSoVezQuandoVariosCamposBatem() {
		Long autorDom = autorRepository.save(autorEntidade(nome("Dom Autor"))).getId();
		Long categoriaId = criarCategoria(nome("Dom Categoria"));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorDom, categoriaId, null));

		assertThat(livroService.listar(null, "dom", autorDom, null)).extracting(LivroResponse::titulo)
				.containsExactly("Dom Casmurro");
	}

	@Test
	void buscaCombinaComOsFiltrosDeTituloAutorECategoria() {
		Long machado = criarAutor();
		Long outroAutor = autorRepository.save(autorEntidade("José de Alencar")).getId();
		Long literatura = criarCategoria(nome("Literatura"));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, machado, literatura, null));
		livroService.criar(new LivroRequest("Dom Sebastião", "9780316000000", 1899, null, null, outroAutor, literatura, null));
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780321000000", 1881, null, null, machado, null, null));

		// busca + autorId
		assertThat(livroService.listar(null, "1899", machado, null)).extracting(LivroResponse::titulo)
				.containsExactly("Dom Casmurro");
		// busca + categoriaId
		assertThat(livroService.listar(null, "1899", machado, literatura)).extracting(LivroResponse::titulo)
				.containsExactly("Dom Casmurro");
		// busca + titulo: os dois precisam bater
		assertThat(livroService.listar("memórias", "1899", machado, null)).isEmpty();
		assertThat(livroService.listar("memórias", "1881", machado, null)).extracting(LivroResponse::titulo)
				.containsExactly("Memórias Póstumas");
	}

	@Test
	void buscaComCuringaDoLikeProcuraOCaractereComoTexto() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("100% Jovem", "9780316000000", null, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Guia_de_Estudos", "8535910654", null, null, null, autorId, null, null));

		assertThat(livroService.listar(null, "%", autorId, null)).extracting(LivroResponse::titulo)
				.containsExactly("100% Jovem");
		assertThat(livroService.listar(null, "_", autorId, null)).extracting(LivroResponse::titulo)
				.containsExactly("Guia_de_Estudos");
		assertThat(livroService.listar(null, "\\", autorId, null)).isEmpty();
	}

	@Test
	void buscaVaziaOuEmBrancoNaoFiltra() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId, null, null));
		livroService.criar(new LivroRequest("Senhora", "9780316000000", 1875, null, null, autorId, null, null));

		assertThat(livroService.listar(null, "", autorId, null)).hasSize(2);
		assertThat(livroService.listar(null, "   ", autorId, null)).hasSize(2);
		assertThat(livroService.listar(null, null, autorId, null)).hasSize(2);
	}

	@Test
	void buscaIgnoraEspacosNasPontas() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId, null, null));

		assertThat(livroService.listar(null, "  casmurro  ", autorId, null)).extracting(LivroResponse::titulo)
				.containsExactly("Dom Casmurro");
	}

}
