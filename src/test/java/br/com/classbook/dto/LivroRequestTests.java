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

/**
 * Confere a normalização e as regras de Bean Validation de {@link LivroRequest}, sem banco e sem
 * Spring: o mesmo que o {@code @Valid} do controller vai aplicar.
 */
class LivroRequestTests {

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
		LivroRequest request = new LivroRequest("Dom Casmurro", "978-0-306-40615-7", 1899, "Descrição",
				"Editora Exemplo", 1L);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void apenasTituloIsbnEAutorIdSaoObrigatorios() {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, 1L);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void tituloIsbnEAutorIdAusentesSaoInvalidos() {
		LivroRequest request = new LivroRequest(null, null, null, null, null, null);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("titulo", "isbn", "autorId");
	}

	@Test
	void isbnEhNormalizadoSemHifensEEmMaiusculas() {
		LivroRequest request = new LivroRequest("Título", "978-0-306-40615-x", null, null, null, 1L);

		assertThat(request.isbn()).isEqualTo("978030640615X");
	}

	@Test
	void isbnForaDoFormatoEhInvalido() {
		LivroRequest request = new LivroRequest("Título", "123", null, null, null, 1L);

		assertThat(camposInvalidos(request)).containsExactly("isbn");
	}

	@Test
	void aceitaOsTamanhosMaximosDasColunas() {
		LivroRequest request = new LivroRequest("a".repeat(200), "9780306406157", null, "a".repeat(2000),
				"a".repeat(120), 1L);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void rejeitaTextoMaiorQueAColuna() {
		LivroRequest request = new LivroRequest("a".repeat(201), "9780306406157", null, null, "a".repeat(121), 1L);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("titulo", "editora");
	}

	private Set<String> camposInvalidos(LivroRequest request) {
		return validator.validate(request).stream()
				.map(violacao -> violacao.getPropertyPath().toString())
				.collect(Collectors.toSet());
	}

}
