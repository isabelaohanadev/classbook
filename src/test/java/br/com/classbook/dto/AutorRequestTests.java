package br.com.classbook.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Confere as regras de Bean Validation de {@link AutorRequest}, sem banco e sem Spring: o mesmo que o
 * {@code @Valid} do controller vai aplicar.
 */
class AutorRequestTests {

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
		AutorRequest request = new AutorRequest("Machado de Assis", "Brasileira", LocalDate.of(1839, 6, 21),
				"Romancista, contista e poeta brasileiro.");

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void somenteNomeEhObrigatorio() {
		AutorRequest request = new AutorRequest("Machado de Assis", null, null, null);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void nomeAusenteOuEmBrancoEhInvalido() {
		assertThat(camposInvalidos(new AutorRequest(null, null, null, null))).containsExactly("nome");
		assertThat(camposInvalidos(new AutorRequest("   ", null, null, null))).containsExactly("nome");
	}

	@Test
	void dataDeNascimentoNoFuturoEhInvalida() {
		AutorRequest request = new AutorRequest("Autor Futurista", null, LocalDate.now().plusDays(1), null);

		assertThat(camposInvalidos(request)).containsExactly("dataNascimento");
	}

	@Test
	void aceitaOsTamanhosMaximosDasColunas() {
		AutorRequest request = new AutorRequest("a".repeat(150), "a".repeat(80), null, "a".repeat(1000));

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void rejeitaTextoMaiorQueAColuna() {
		AutorRequest request = new AutorRequest("a".repeat(151), "a".repeat(81), null, null);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("nome", "nacionalidade");
	}

	private Set<String> camposInvalidos(AutorRequest request) {
		return validator.validate(request).stream()
				.map(violacao -> violacao.getPropertyPath().toString())
				.collect(Collectors.toSet());
	}

}
