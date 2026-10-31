package br.com.classbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.LivroRequest;
import br.com.classbook.dto.LivroResponse;
import br.com.classbook.exception.DadosInvalidosException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Autor;
import br.com.classbook.repository.AutorRepository;
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

	private Long autorId;

	private Long criarAutor() {
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		return autorRepository.save(autor).getId();
	}

	@Test
	void criarGravaERetornaOLivroComOAutor() {
		autorId = criarAutor();

		LivroResponse response = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId));

		assertThat(response.id()).isNotNull();
		assertThat(response.titulo()).isEqualTo("Dom Casmurro");
		assertThat(response.autor().id()).isEqualTo(autorId);
		assertThat(livroRepository.findById(response.id())).isPresent();
	}

	@Test
	void criarComAutorInexistenteLancaDadosInvalidos() {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, -1L);

		assertThatThrownBy(() -> livroService.criar(request)).isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void listarRetornaTodosOrdenadosPorTitulo() {
		autorId = criarAutor();
		livroService.criar(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId));
		livroService.criar(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId));

		List<LivroResponse> livros = livroService.listar();

		assertThat(livros).extracting(LivroResponse::titulo).containsExactly("Dom Casmurro", "Memórias Póstumas");
	}

	@Test
	void buscarPorIdRetornaOLivroExistente() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId));

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
				.criar(new LivroRequest("Título Antigo", "9780306406157", null, null, null, autorId));

		LivroResponse atualizado = livroService.atualizar(criado.id(),
				new LivroRequest("Título Novo", "9780316000000", 2020, "Nova descrição", "Nova editora", autorId));

		assertThat(atualizado.titulo()).isEqualTo("Título Novo");
		assertThat(atualizado.isbn()).isEqualTo("9780316000000");
		assertThat(atualizado.anoPublicacao()).isEqualTo(2020);
	}

	@Test
	void atualizarComIdInexistenteLancaExcecao() {
		autorId = criarAutor();
		LivroRequest request = new LivroRequest("Título", "9780306406157", null, null, null, autorId);

		assertThatThrownBy(() -> livroService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarComAutorInexistenteLancaDadosInvalidos() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, -1L);

		assertThatThrownBy(() -> livroService.atualizar(criado.id(), edicao))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void excluirRemoveOLivro() {
		autorId = criarAutor();
		LivroResponse criado = livroService
				.criar(new LivroRequest("Título", "9780306406157", null, null, null, autorId));

		livroService.excluir(criado.id());

		assertThat(livroRepository.findById(criado.id())).isEmpty();
	}

	@Test
	void excluirComIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> livroService.excluir(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

}
