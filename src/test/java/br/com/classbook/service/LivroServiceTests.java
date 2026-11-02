package br.com.classbook.service;

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
import br.com.classbook.model.TipoCategoria;
import br.com.classbook.repository.AutorRepository;
import br.com.classbook.repository.CategoriaRepository;
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

	private Long autorId;

	private Long criarAutor() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		return autorRepository.save(autor).getId();
	}

	private Long criarCategoria(String nome) {
		Categoria categoria = new Categoria();
		categoria.setNome(nome);
		categoria.setTipo(TipoCategoria.LITERATURA);
		return categoriaRepository.save(categoria).getId();
	}

	@Test
	void criarGravaERetornaOLivroComOAutor() {
		autorId = criarAutor();

		LivroResponse response = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId, null));

		assertThat(response.id()).isNotNull();
		assertThat(response.titulo()).isEqualTo("Dom Casmurro");
		assertThat(response.autor().id()).isEqualTo(autorId);
		assertThat(livroRepository.findById(response.id())).isPresent();
	}

	@Test
	void criarComAnoDePublicacaoNoFuturoLancaDadosInvalidos() {
		autorId = criarAutor();
		int anoQueVem = Year.now().getValue() + 1;
		LivroRequest request = new LivroRequest("Livro do Futuro", "9780306406157", anoQueVem, null, null, autorId, null);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class)
				.hasMessage("O ano de publicação " + anoQueVem + " está no futuro.");
	}

	@Test
	void anoDePublicacaoAtualEhAceito() {
		autorId = criarAutor();
		int anoAtual = Year.now().getValue();

		LivroResponse response = livroService
				.criar(new LivroRequest("Lançamento", "9780306406157", anoAtual, null, null, autorId, null));

		assertThat(response.anoPublicacao()).isEqualTo(anoAtual);
	}

	@Test
	void atualizarComAnoDePublicacaoNoFuturoLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", Year.now().getValue() + 1, null, null,
				autorId, null);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void criarComAutorInexistenteLancaDadosInvalidos() {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, -1L, null);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void criarComIsbnJaCadastradoLancaConflito() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null));
		LivroRequest duplicado = new LivroRequest("Outro Título", "9780306406157", null, null, null, autorId, null);

		assertThatThrownBy(() -> livroService.criar(duplicado)).isInstanceOf(ConflitoException.class)
				.hasMessage("Já existe um livro com o ISBN 9780306406157.");
	}

	@Test
	void criarComIsbnJaCadastradoComHifensDiferentesLancaConflito() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "978-0-306-40615-7", null, null, null, autorId, null));
		LivroRequest duplicado = new LivroRequest("Outro Título", "9780306406157", null, null, null, autorId, null);

		assertThatThrownBy(() -> livroService.criar(duplicado)).isInstanceOf(ConflitoException.class);
	}

	@Test
	void listarRetornaTodosOrdenadosPorTitulo() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId, null));

		List<LivroResponse> livros = livroService.listar(null);

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro", "Memórias Póstumas");
	}

	@Test
	void listarComTituloFiltraSemDiferenciarMaiusculas() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId, null));

		List<LivroResponse> livros = livroService.listar("casmurro");

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro");
	}

	@Test
	void listarComTituloSemCorrespondenciaRetornaListaVazia() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null));

		assertThat(livroService.listar("inexistente")).isEmpty();
	}

	@Test
	void buscarPorIdRetornaOLivroExistente() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null));

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
				.criar(new LivroRequest("Título Antigo", "9780306406157", null, null, null, autorId, null));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título Novo", "9780316000000", 2020, "Nova descrição", "Nova editora", autorId, null));

		assertThat(atualizado.titulo()).isEqualTo("Título Novo");
		assertThat(atualizado.isbn()).isEqualTo("9780316000000");
		assertThat(atualizado.anoPublicacao()).isEqualTo(2020);
	}

	@Test
	void atualizarComIdInexistenteLancaExcecao() {
		autorId = criarAutor();
		LivroRequest request = new LivroRequest("Título", "9780306406157", null, null, null, autorId, null);

		assertThatThrownBy(() -> livroService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarComAutorInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, -1L, null);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void atualizarComIsbnDeOutroLivroLancaConflito() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null));
		LivroResponse outro = livroService
				.criar(new LivroRequest("Memórias Póstumas", "9780316000000", null, null, null, autorId, null));
		LivroRequest edicao = new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null);

		assertThatThrownBy(() -> livroService.atualizar(outro.id(), edicao)).isInstanceOf(ConflitoException.class)
				.hasMessage("Já existe um livro com o ISBN 9780306406157.");
	}

	@Test
	void atualizarMantendoOProprioIsbnNaoEhConflito() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título Antigo", "9780306406157", null, null, null, autorId, null));
		LivroRequest edicao = new LivroRequest("Título Novo", "9780306406157", null, null, null, autorId, null);

		LivroResponse atualizado = livroService.atualizar(criado.id(), edicao);

		assertThat(atualizado.titulo()).isEqualTo("Título Novo");
	}

	@Test
	void excluirRemoveOLivro() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null));

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
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null));

		assertThat(response.categoria()).isNull();
	}

	@Test
	void criarComCategoriaValidaRetornaOLivroComACategoria() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Literatura"));

		LivroResponse response = livroService.criar(
				new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, categoriaId));

		assertThat(response.categoria().id()).isEqualTo(categoriaId);
		assertThat(response.categoria().nome()).isEqualTo(nome("Literatura"));
	}

	@Test
	void criarComCategoriaInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, -1L);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void atualizarComCategoriaValidaAtualizaACategoriaDoLivro() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Literatura"));
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId));

		assertThat(atualizado.categoria().id()).isEqualTo(categoriaId);
	}

	@Test
	void atualizarSemCategoriaIdRemoveACategoriaDoLivro() {
		autorId = criarAutor();
		Long categoriaId = criarCategoria(nome("Literatura"));
		LivroResponse criado = livroService.criar(
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null));

		assertThat(atualizado.categoria()).isNull();
	}

	@Test
	void atualizarComCategoriaInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId, -1L);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

}
