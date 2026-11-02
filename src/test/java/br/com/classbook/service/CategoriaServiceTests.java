package br.com.classbook.service;

import static br.com.classbook.Unico.nome;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.dto.CategoriaResponse;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Autor;
import br.com.classbook.model.Livro;
import br.com.classbook.model.TipoCategoria;
import br.com.classbook.repository.AutorRepository;
import br.com.classbook.repository.CategoriaRepository;
import br.com.classbook.repository.LivroRepository;

/**
 * Confere as regras de negócio de {@link CategoriaService}. A transação é desfeita ao final de cada
 * teste.
 */
@SpringBootTest
@Transactional
class CategoriaServiceTests {

	@Autowired
	private CategoriaService categoriaService;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private AutorRepository autorRepository;

	@Autowired
	private LivroRepository livroRepository;

	@Test
	void criarGravaERetornaACategoria() {
		CategoriaResponse response = categoriaService.criar(new CategoriaRequest(nome("Literatura"),
				"Romances, contos e poesia", "14 a 17 anos", TipoCategoria.LITERATURA));

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new CategoriaResponse(response.id(), nome("Literatura"),
				"Romances, contos e poesia", "14 a 17 anos", TipoCategoria.LITERATURA));
		assertThat(categoriaRepository.findById(response.id())).isPresent();
	}

	@Test
	void listarRetornaOrdenadasPorNome() {
		categoriaService.criar(new CategoriaRequest(nome("Referência"), null, null, TipoCategoria.REFERENCIA));
		categoriaService.criar(new CategoriaRequest(nome("Didáticos"), null, null, TipoCategoria.DIDATICO));
		categoriaService.criar(new CategoriaRequest(nome("Literatura"), null, null, TipoCategoria.LITERATURA));

		// Confere a ordem relativa, sem supor que o banco de teste começa vazio.
		assertThat(categoriaService.listar()).extracting(CategoriaResponse::nome)
				.containsSubsequence(nome("Didáticos"), nome("Literatura"), nome("Referência"));
	}

	@Test
	void buscarPorIdRetornaACategoriaExistente() {
		CategoriaResponse criada = categoriaService
				.criar(new CategoriaRequest(nome("Literatura"), null, null, TipoCategoria.LITERATURA));

		assertThat(categoriaService.buscarPorId(criada.id())).isEqualTo(criada);
	}

	@Test
	void buscarPorIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> categoriaService.buscarPorId(-1L))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarSubstituiOsCamposDaCategoria() {
		CategoriaResponse criada = categoriaService
				.criar(new CategoriaRequest(nome("Nome Antigo"), "Descrição antiga", "12 a 14 anos",
					TipoCategoria.OUTRO));

		CategoriaResponse atualizada = categoriaService.atualizar(criada.id(),
				new CategoriaRequest(nome("Nome Novo"), null, "15 a 17 anos", TipoCategoria.PARADIDATICO));

		assertThat(atualizada).isEqualTo(new CategoriaResponse(criada.id(), nome("Nome Novo"), null, "15 a 17 anos",
				TipoCategoria.PARADIDATICO));
	}

	@Test
	void atualizarIdInexistenteLancaExcecao() {
		CategoriaRequest request = new CategoriaRequest(nome("Nome"), null, null, TipoCategoria.OUTRO);

		assertThatThrownBy(() -> categoriaService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirRemoveACategoria() {
		CategoriaResponse criada = categoriaService
				.criar(new CategoriaRequest(nome("Categoria Descartável"), null, null, TipoCategoria.OUTRO));

		categoriaService.excluir(criada.id());

		assertThat(categoriaRepository.findById(criada.id())).isEmpty();
	}

	@Test
	void excluirIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> categoriaService.excluir(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirCategoriaComLivrosVinculadosLancaConflito() {
		CategoriaResponse categoria = categoriaService
				.criar(new CategoriaRequest(nome("Literatura"), null, null, TipoCategoria.LITERATURA));
		Autor autor = new Autor();
		autor.setNome("Machado de Assis");
		autorRepository.save(autor);
		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAutor(autor);
		livro.setCategoria(categoriaRepository.findById(categoria.id()).orElseThrow());
		livroRepository.save(livro);

		assertThatThrownBy(() -> categoriaService.excluir(categoria.id())).isInstanceOf(ConflitoException.class);
		assertThat(categoriaRepository.findById(categoria.id())).isPresent();
	}

	@Test
	void criarComNomeJaUsadoLancaConflitoSemDiferenciarMaiusculas() {
		categoriaService.criar(new CategoriaRequest(nome("Literatura"), null, null, TipoCategoria.LITERATURA));
		CategoriaRequest duplicada = new CategoriaRequest(nome("LITERATURA"), null, null, TipoCategoria.OUTRO);

		assertThatThrownBy(() -> categoriaService.criar(duplicada)).isInstanceOf(ConflitoException.class);
	}

	@Test
	void atualizarComNomeDeOutraCategoriaLancaConflito() {
		categoriaService.criar(new CategoriaRequest(nome("Literatura"), null, null, TipoCategoria.LITERATURA));
		CategoriaResponse outra = categoriaService
				.criar(new CategoriaRequest(nome("Referência"), null, null, TipoCategoria.REFERENCIA));
		CategoriaRequest edicao = new CategoriaRequest(nome("literatura"), null, null, TipoCategoria.REFERENCIA);

		assertThatThrownBy(() -> categoriaService.atualizar(outra.id(), edicao))
				.isInstanceOf(ConflitoException.class);
	}

	@Test
	void atualizarMantendoOProprioNomeNaoEhConflito() {
		CategoriaResponse criada = categoriaService
				.criar(new CategoriaRequest(nome("literatura"), null, null, TipoCategoria.LITERATURA));

		CategoriaResponse atualizada = categoriaService.atualizar(criada.id(),
				new CategoriaRequest(nome("Literatura"), null, null, TipoCategoria.LITERATURA));

		assertThat(atualizada.nome()).isEqualTo(nome("Literatura"));
	}

}
