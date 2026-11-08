package br.com.classbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolationException;

import tools.jackson.core.JacksonException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.classbook.Json;
import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.dto.LivroRequest;
import br.com.classbook.exception.DadosInvalidosException;
import br.com.classbook.model.TipoCategoria;

/**
 * Confere a mesclagem do {@code PATCH} (JSON Merge Patch) com {@link MescladorDePatch}, sem tocar no
 * banco: o campo ausente mantém o valor, o {@code null} limpa, e o resultado passa pela normalização e
 * pelo Bean Validation do {@code Request}.
 */
@SpringBootTest
class MescladorDePatchTests {

	@Autowired
	private MescladorDePatch mesclador;

	private static LivroRequest livro() {
		return new LivroRequest("Dom Casmurro", "9780306406157", 1899, "Resumo", "Editora A", 1L, 2L, 3L);
	}

	@Test
	void camposAusentesMantemOValorAtual() {
		LivroRequest resultado = mesclador.mesclar(livro(), Json.de("{}"), LivroRequest.class);

		assertThat(resultado).isEqualTo(livro());
	}

	@Test
	void campoEnviadoSubstituiOValorEOsDemaisFicam() {
		LivroRequest resultado = mesclador.mesclar(livro(), Json.de("{'titulo': 'Memórias Póstumas', 'anoPublicacao': 1881}"),
				LivroRequest.class);

		assertThat(resultado).isEqualTo(new LivroRequest("Memórias Póstumas", "9780306406157", 1881, "Resumo",
				"Editora A", 1L, 2L, 3L));
	}

	@Test
	void nullLimpaOsCamposOpcionais() {
		LivroRequest resultado = mesclador.mesclar(livro(),
				Json.de("{'anoPublicacao': null, 'descricao': null, 'editora': null, 'categoriaId': null, "
						+ "'disciplinaId': null}"),
				LivroRequest.class);

		assertThat(resultado).isEqualTo(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, 1L, null,
				null));
	}

	@Test
	void nullEmCampoObrigatorioViolaAValidacaoApontandoOCampo() {
		assertThatThrownBy(() -> mesclador.mesclar(livro(), Json.de("{'titulo': null, 'autorId': null}"),
				LivroRequest.class))
				.isInstanceOfSatisfying(ConstraintViolationException.class, ex -> assertThat(ex.getConstraintViolations())
						.extracting(violacao -> violacao.getPropertyPath().toString())
						.containsExactlyInAnyOrder("titulo", "autorId"));
	}

	@Test
	void valorMescladoQueViolaORequestEhRejeitado() {
		String tituloLongo = "a".repeat(201);

		assertThatThrownBy(() -> mesclador.mesclar(livro(), Json.de("{'titulo': '" + tituloLongo + "'}"),
				LivroRequest.class)).isInstanceOf(ConstraintViolationException.class);
		assertThatThrownBy(() -> mesclador.mesclar(livro(), Json.de("{'isbn': '123'}"), LivroRequest.class))
				.isInstanceOf(ConstraintViolationException.class);
	}

	@Test
	void normalizacaoDoConstrutorDoRequestValeParaOResultado() {
		LivroRequest livro = mesclador.mesclar(livro(), Json.de("{'isbn': '978-0-316-00000-0'}"), LivroRequest.class);
		CategoriaRequest categoria = mesclador.mesclar(
				new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA),
				Json.de("{'nome': '  Referência  '}"), CategoriaRequest.class);

		assertThat(livro.isbn()).isEqualTo("9780316000000");
		assertThat(categoria.nome()).isEqualTo("Referência");
	}

	@Test
	void idECamposDesconhecidosSaoIgnorados() {
		LivroRequest resultado = mesclador.mesclar(livro(),
				Json.de("{'id': 99, 'inexistente': 'x', 'autor': {'id': 7}, 'titulo': 'Novo'}"), LivroRequest.class);

		assertThat(resultado).isEqualTo(new LivroRequest("Novo", "9780306406157", 1899, "Resumo", "Editora A", 1L, 2L,
				3L));
	}

	@Test
	void corpoQueNaoEhObjetoLancaDadosInvalidos() {
		for (String corpo : new String[] { "[]", "[{'titulo': 'x'}]", "'texto'", "42", "true", "null" }) {
			assertThatThrownBy(() -> mesclador.mesclar(livro(), Json.de(corpo), LivroRequest.class))
					.as(corpo).isInstanceOf(DadosInvalidosException.class);
		}
		assertThatThrownBy(() -> mesclador.mesclar(livro(), null, LivroRequest.class))
				.isInstanceOf(DadosInvalidosException.class);
	}

	@Test
	void valorDeTipoErradoLancaErroDoJacksonComOCaminhoDoCampo() {
		assertThatThrownBy(() -> mesclador.mesclar(livro(), Json.de("{'anoPublicacao': 'abc'}"), LivroRequest.class))
				.isInstanceOfSatisfying(JacksonException.class, ex -> assertThat(ex.getPath())
						.extracting(JacksonException.Reference::getPropertyName).containsExactly("anoPublicacao"));
		assertThatThrownBy(() -> mesclador.mesclar(
				new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA), Json.de("{'tipo': 'INFANTIL'}"),
				CategoriaRequest.class)).isInstanceOf(JacksonException.class);
	}

	@Test
	void naoAlteraORequestAtual() {
		LivroRequest atual = livro();

		mesclador.mesclar(atual, Json.de("{'titulo': 'Outro', 'categoriaId': null}"), LivroRequest.class);

		assertThat(atual).isEqualTo(livro());
	}

}
