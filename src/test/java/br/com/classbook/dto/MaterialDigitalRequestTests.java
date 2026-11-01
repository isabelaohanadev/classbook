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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.classbook.model.FormatoMaterial;

/**
 * Confere as regras de Bean Validation de {@link MaterialDigitalRequest}, sem banco e sem Spring: o
 * mesmo que o {@code @Valid} do controller vai aplicar.
 */
class MaterialDigitalRequestTests {

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
		MaterialDigitalRequest request = new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF,
				"https://exemplo.com/funcoes.pdf", "Texto opcional", LocalDate.of(2026, 3, 10), 1L);

		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void descricaoEDataDePublicacaoSaoOpcionais() {
		assertThat(camposInvalidos(comUrl("https://exemplo.com/funcoes.pdf"))).isEmpty();
	}

	@Test
	void tituloFormatoUrlEDisciplinaSaoObrigatorios() {
		MaterialDigitalRequest request = new MaterialDigitalRequest(null, null, null, null, null, null);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("titulo", "formato", "url", "disciplinaId");
	}

	@ParameterizedTest
	@ValueSource(strings = { "http://exemplo.com", "https://exemplo.com/funcoes.pdf",
			"HTTPS://EXEMPLO.COM/VIDEO", "https://exemplo.com:8080/a?b=1#c" })
	void aceitaUrlHttpOuHttps(String url) {
		assertThat(camposInvalidos(comUrl(url))).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = { "exemplo.com/funcoes.pdf", "ftp://exemplo.com/funcoes.pdf", "mailto:a@exemplo.com",
			"https://exemplo.com/com espaço", "http://", "   " })
	void rejeitaUrlQueNaoEhHttpOuHttps(String url) {
		assertThat(camposInvalidos(comUrl(url))).containsExactly("url");
	}

	@Test
	void aceitaOsTamanhosMaximosDasColunas() {
		String url = "https://exemplo.com/" + "a".repeat(480);
		MaterialDigitalRequest request = new MaterialDigitalRequest("a".repeat(200), FormatoMaterial.LINK, url,
				"a".repeat(2000), null, 1L);

		assertThat(url).hasSize(500);
		assertThat(camposInvalidos(request)).isEmpty();
	}

	@Test
	void rejeitaTextoMaiorQueAColuna() {
		MaterialDigitalRequest request = new MaterialDigitalRequest("a".repeat(201), FormatoMaterial.LINK,
				"https://exemplo.com/" + "a".repeat(481), null, null, 1L);

		assertThat(camposInvalidos(request)).containsExactlyInAnyOrder("titulo", "url");
	}

	private static MaterialDigitalRequest comUrl(String url) {
		return new MaterialDigitalRequest("Lista de funções", FormatoMaterial.PDF, url, null, null, 1L);
	}

	private Set<String> camposInvalidos(MaterialDigitalRequest request) {
		return validator.validate(request).stream()
				.map(violacao -> violacao.getPropertyPath().toString())
				.collect(Collectors.toSet());
	}

}
