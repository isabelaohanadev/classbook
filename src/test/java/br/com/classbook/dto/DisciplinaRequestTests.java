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

import br.com.classbook.model.SerieEscolar;

/**
 * Confere a normalização e as regras de Bean Validation de {@link DisciplinaRequest}, sem banco e sem
 * Spring: o mesmo que o {@code @Valid} do controller vai aplicar.
 */
class DisciplinaRequestTests {

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
		DisciplinaRequest request = new DisciplinaRequest("Matemática", "MAT-1", "Matemática da 1ª série",
				SerieEscolar.PRIMEIRA_SERIE);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void descricaoEhOpcional() {
		DisciplinaRequest request = new DisciplinaRequest("Matemática", "MAT-1", null, SerieEscolar.PRIMEIRA_SERIE);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void nomeCodigoESerieEscolarSaoObrigatorios() {
		DisciplinaRequest request = new DisciplinaRequest(null, null, null, null);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("nome", "codigo", "serieEscolar");
	}

	@Test
	void codigoChegaSemEspacosNasPontasEEmMaiusculas() {
		DisciplinaRequest request = new DisciplinaRequest("Matemática", "  mat-1 \t", null,
				SerieEscolar.PRIMEIRA_SERIE);

		assertThat(request.codigo()).isEqualTo("MAT-1");
	}

	@Test
	void nomeOuCodigoSoComEspacosSaoInvalidos() {
		DisciplinaRequest request = new DisciplinaRequest("   ", "   ", null, SerieEscolar.PRIMEIRA_SERIE);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("nome", "codigo");
	}

	@Test
	void aceitaOsTamanhosMaximosDasColunas() {
		DisciplinaRequest request = new DisciplinaRequest("a".repeat(100), "A".repeat(20), "a".repeat(500),
				SerieEscolar.SEGUNDA_SERIE);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void rejeitaTextoMaiorQueAColuna() {
		DisciplinaRequest request = new DisciplinaRequest("a".repeat(101), "A".repeat(21), "a".repeat(501),
				SerieEscolar.SEGUNDA_SERIE);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("nome", "codigo", "descricao");
	}

	private Set<String> camposInvalidos(DisciplinaRequest request) {
		return validator.validate(request).stream()
				.map(violacao -> violacao.getPropertyPath().toString())
				.collect(Collectors.toSet());
	}

}
