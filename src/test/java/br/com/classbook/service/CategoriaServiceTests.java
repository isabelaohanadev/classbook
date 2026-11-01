package br.com.classbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.dto.CategoriaResponse;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.TipoCategoria;
import br.com.classbook.repository.CategoriaRepository;

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

	@Test
	void criarGravaERetornaACategoria() {
		CategoriaResponse response = categoriaService.criar(new CategoriaRequest("Literatura",
				"Romances, contos e poesia", "14 a 17 anos", TipoCategoria.LITERATURA));

		assertThat(response.id()).isNotNull();
		assertThat(response).isEqualTo(new CategoriaResponse(response.id(), "Literatura", "Romances, contos e poesia",
				"14 a 17 anos", TipoCategoria.LITERATURA));
		assertThat(categoriaRepository.findById(response.id())).isPresent();
	}

	@Test
	void listarRetornaOrdenadasPorNome() {
		categoriaService.criar(new CategoriaRequest("Referência", null, null, TipoCategoria.REFERENCIA));
		categoriaService.criar(new CategoriaRequest("Didáticos", null, null, TipoCategoria.DIDATICO));
		categoriaService.criar(new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA));

		// Confere a ordem relativa, sem supor que o banco de teste começa vazio.
		assertThat(categoriaService.listar()).extracting(CategoriaResponse::nome)
				.containsSubsequence("Didáticos", "Literatura", "Referência");
	}

	@Test
	void buscarPorIdRetornaACategoriaExistente() {
		CategoriaResponse criada = categoriaService
				.criar(new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA));

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
				.criar(new CategoriaRequest("Nome Antigo", "Descrição antiga", "12 a 14 anos", TipoCategoria.OUTRO));

		CategoriaResponse atualizada = categoriaService.atualizar(criada.id(),
				new CategoriaRequest("Nome Novo", null, "15 a 17 anos", TipoCategoria.PARADIDATICO));

		assertThat(atualizada).isEqualTo(new CategoriaResponse(criada.id(), "Nome Novo", null, "15 a 17 anos",
				TipoCategoria.PARADIDATICO));
	}

	@Test
	void atualizarIdInexistenteLancaExcecao() {
		CategoriaRequest request = new CategoriaRequest("Nome", null, null, TipoCategoria.OUTRO);

		assertThatThrownBy(() -> categoriaService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirRemoveACategoria() {
		CategoriaResponse criada = categoriaService
				.criar(new CategoriaRequest("Categoria Descartável", null, null, TipoCategoria.OUTRO));

		categoriaService.excluir(criada.id());

		assertThat(categoriaRepository.findById(criada.id())).isEmpty();
	}

	@Test
	void excluirIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> categoriaService.excluir(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

}
