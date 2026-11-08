package br.com.classbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import tools.jackson.core.JacksonException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.Json;
import br.com.classbook.dto.AutorRequest;
import br.com.classbook.dto.AutorResponse;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Livro;
import br.com.classbook.repository.AutorRepository;
import br.com.classbook.repository.LivroRepository;

/**
 * Confere as regras de negócio de {@link AutorService}. A transação é desfeita ao final de cada
 * teste.
 */
@SpringBootTest
@Transactional
class AutorServiceTests {

	@Autowired
	private AutorService autorService;

	@Autowired
	private AutorRepository autorRepository;

	@Autowired
	private LivroRepository livroRepository;

	@Test
	void criarGravaERetornaOAutor() {
		AutorResponse response = autorService.criar(new AutorRequest("Cecília Meireles", "Brasileira", null, null));

		assertThat(response.id()).isNotNull();
		assertThat(response.nome()).isEqualTo("Cecília Meireles");
		assertThat(autorRepository.findById(response.id())).isPresent();
	}

	@Test
	void listarRetornaTodosOrdenadosPorNome() {
		autorService.criar(new AutorRequest("Machado de Assis", null, null, null));
		autorService.criar(new AutorRequest("Clarice Lispector", null, null, null));
		autorService.criar(new AutorRequest("Ariano Suassuna", null, null, null));

		List<AutorResponse> autores = autorService.listar();

		assertThat(autores).extracting(AutorResponse::nome)
				.containsExactly("Ariano Suassuna", "Clarice Lispector", "Machado de Assis");
	}

	@Test
	void listarSemAutoresRetornaListaVazia() {
		assertThat(autorService.listar()).isEmpty();
	}

	@Test
	void buscarPorIdRetornaOAutorExistente() {
		AutorResponse criado = autorService.criar(new AutorRequest("José de Alencar", null, null, null));

		AutorResponse encontrado = autorService.buscarPorId(criado.id());

		assertThat(encontrado).isEqualTo(criado);
	}

	@Test
	void buscarPorIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> autorService.buscarPorId(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void atualizarSubstituiOsCamposDoAutor() {
		AutorResponse criado = autorService.criar(new AutorRequest("Nome Antigo", "Brasileira", null, null));

		AutorResponse atualizado = autorService.atualizar(criado.id(),
				new AutorRequest("Nome Novo", "Portuguesa", null, "Nova biografia"));

		assertThat(atualizado.nome()).isEqualTo("Nome Novo");
		assertThat(atualizado.nacionalidade()).isEqualTo("Portuguesa");
		assertThat(atualizado.biografia()).isEqualTo("Nova biografia");
	}

	@Test
	void atualizarIdInexistenteLancaExcecao() {
		AutorRequest request = new AutorRequest("Nome", null, null, null);

		assertThatThrownBy(() -> autorService.atualizar(-1L, request))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirRemoveOAutor() {
		AutorResponse criado = autorService.criar(new AutorRequest("Autor Descartável", null, null, null));

		autorService.excluir(criado.id());

		assertThat(autorRepository.findById(criado.id())).isEmpty();
	}

	@Test
	void atualizarParcialmenteMudaSoOsCamposEnviados() {
		AutorResponse criado = autorService.criar(new AutorRequest("Machado de Assis", "Brasileira",
				LocalDate.of(1839, 6, 21), "Fundador"));

		AutorResponse atualizado = autorService.atualizarParcialmente(criado.id(),
				Json.de("{'biografia': 'Fundador da Academia Brasileira de Letras'}"));

		assertThat(atualizado).isEqualTo(new AutorResponse(criado.id(), "Machado de Assis", "Brasileira",
				LocalDate.of(1839, 6, 21), "Fundador da Academia Brasileira de Letras"));
		assertThat(autorRepository.findById(criado.id()).orElseThrow().getBiografia())
				.isEqualTo("Fundador da Academia Brasileira de Letras");
	}

	@Test
	void atualizarParcialmenteComNullLimpaOsCamposOpcionais() {
		AutorResponse criado = autorService.criar(new AutorRequest("Machado de Assis", "Brasileira",
				LocalDate.of(1839, 6, 21), "Fundador"));

		AutorResponse atualizado = autorService.atualizarParcialmente(criado.id(),
				Json.de("{'dataNascimento': null, 'biografia': null}"));

		assertThat(atualizado).isEqualTo(new AutorResponse(criado.id(), "Machado de Assis", "Brasileira", null, null));
	}

	@Test
	void atualizarParcialmenteComNomeEmBrancoLancaViolacaoDeValidacao() {
		AutorResponse criado = autorService.criar(new AutorRequest("Machado de Assis", null, null, null));

		assertThatThrownBy(() -> autorService.atualizarParcialmente(criado.id(), Json.de("{'nome': '   '}")))
				.isInstanceOfSatisfying(ConstraintViolationException.class, ex -> assertThat(ex.getConstraintViolations())
						.extracting(violacao -> violacao.getPropertyPath().toString()).containsExactly("nome"));
		assertThat(autorRepository.findById(criado.id()).orElseThrow().getNome()).isEqualTo("Machado de Assis");
	}

	@Test
	void atualizarParcialmenteComDataDeNascimentoNoFuturoLancaViolacaoDeValidacao() {
		AutorResponse criado = autorService.criar(new AutorRequest("Machado de Assis", null, null, null));

		assertThatThrownBy(() -> autorService.atualizarParcialmente(criado.id(),
				Json.de("{'dataNascimento': '2999-01-01'}"))).isInstanceOf(ConstraintViolationException.class);
	}

	@Test
	void atualizarParcialmenteComDataForaDoFormatoLancaErroDoJackson() {
		AutorResponse criado = autorService.criar(new AutorRequest("Machado de Assis", null, null, null));

		assertThatThrownBy(() -> autorService.atualizarParcialmente(criado.id(),
				Json.de("{'dataNascimento': '21/06/1839'}"))).isInstanceOf(JacksonException.class);
	}

	@Test
	void atualizarParcialmenteComIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> autorService.atualizarParcialmente(-1L, Json.de("{'nome': 'Novo'}")))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirIdInexistenteLancaExcecao() {
		assertThatThrownBy(() -> autorService.excluir(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void excluirAutorComLivrosVinculadosLancaConflito() {
		AutorResponse autor = autorService.criar(new AutorRequest("Machado de Assis", null, null, null));
		Livro livro = new Livro();
		livro.setTitulo("Dom Casmurro");
		livro.setIsbn("9780306406157");
		livro.setAutor(autorRepository.findById(autor.id()).orElseThrow());
		livroRepository.save(livro);

		assertThatThrownBy(() -> autorService.excluir(autor.id())).isInstanceOf(ConflitoException.class);
		assertThat(autorRepository.findById(autor.id())).isPresent();
	}

}
