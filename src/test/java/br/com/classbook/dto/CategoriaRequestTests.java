package br.com.classbook.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.classbook.model.TipoCategoria;

/**
 * Confere a normalização e as regras de Bean Validation de {@link CategoriaRequest}, sem banco e sem
 * Spring: o mesmo que o {@code @Valid} do controller vai aplicar.
 */
class CategoriaRequestTests {

	private static ValidatorFactory fabrica;

	private static Validator validator;

	@BeforeAll
	static void criarValidator() {
		fabrica = Validation.buildDefaultValidatorFactory();
		validator = fabrica.getValidator();
	}

	@AfterAll
	static void fecharValidator() {
		fabrica.close();
	}

	@Test
	void requestCompletoEhValido() {
		CategoriaRequest request = new CategoriaRequest("Literatura", "Romances, contos e poesia", "14 a 17 anos",
				TipoCategoria.LITERATURA);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void descricaoEFaixaEtariaSaoOpcionais() {
		CategoriaRequest request = new CategoriaRequest("Literatura", null, null, TipoCategoria.LITERATURA);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void nomeETipoSaoObrigatorios() {
		CategoriaRequest request = new CategoriaRequest(null, null, null, null);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("nome", "tipo");
	}

	@Test
	void nomeChegaSemEspacosNasPontas() {
		CategoriaRequest request = new CategoriaRequest("  Literatura \t", null, null, TipoCategoria.LITERATURA);

		assertThat(request.nome()).isEqualTo("Literatura");
	}

	@Test
	void nomeSoComEspacosEhInvalido() {
		CategoriaRequest request = new CategoriaRequest("   ", null, null, TipoCategoria.LITERATURA);

		assertThat(camposInvalidos(request)).containsExactly("nome");
	}

	@Test
	void aceitaOsTamanhosMaximosDasColunas() {
		CategoriaRequest request = new CategoriaRequest("a".repeat(100), "a".repeat(500), "a".repeat(30),
				TipoCategoria.OUTRO);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void rejeitaTextoMaiorQueAColuna() {
		CategoriaRequest request = new CategoriaRequest("a".repeat(101), "a".repeat(501), "a".repeat(31),
				TipoCategoria.OUTRO);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("nome", "descricao", "faixaEtaria");
	}

	private Set<String> camposInvalidos(CategoriaRequest request) {
		return validator.validate(request).stream()
				.map(violacao -> violacao.getPropertyPath().toString())
				.collect(Collectors.toSet());
	}

}
