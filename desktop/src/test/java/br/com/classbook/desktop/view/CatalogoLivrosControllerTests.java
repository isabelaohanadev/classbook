package br.com.classbook.desktop.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.classbook.desktop.api.ApiIndisponivelException;
import br.com.classbook.desktop.api.ErroDaApiException;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.LivroResponse.AutorResumo;
import br.com.classbook.desktop.modelo.LivroResponse.CategoriaResumo;

class CatalogoLivrosControllerTests {

	@Test
	void nomeDaCategoriaUsaUmTracoQuandoOLivroNaoTemCategoria() {
		assertThat(CatalogoLivrosController.nomeDaCategoria(livro(null))).isEqualTo("—");
		assertThat(CatalogoLivrosController.nomeDaCategoria(livro(new CategoriaResumo(2L, "Ficção"))))
				.isEqualTo("Ficção");
	}

	@Test
	void anoUsaUmTracoQuandoOLivroNaoTemAno() {
		LivroResponse semAno = new LivroResponse(2L, "A Hora da Estrela", "9788532508120", null, null, null,
				new AutorResumo(2L, "Clarice Lispector"), null);

		assertThat(CatalogoLivrosController.ano(livro(null))).isEqualTo("1899");
		assertThat(CatalogoLivrosController.ano(semAno)).isEqualTo("—");
	}

	@Test
	void mensagemDeErroUsaAMensagemDaExcecao() {
		ErroDaApiException erroDaApi = new ErroDaApiException(500, "A API respondeu com o código 500.", List.of());
		ApiIndisponivelException apiIndisponivel =
				new ApiIndisponivelException("http://localhost:8080/api", new IOException("conexão recusada"));

		assertThat(CatalogoLivrosController.mensagemDeErro(erroDaApi)).isEqualTo(erroDaApi.getMessage());
		assertThat(CatalogoLivrosController.mensagemDeErro(apiIndisponivel)).isEqualTo(apiIndisponivel.getMessage());
	}

	private static LivroResponse livro(CategoriaResumo categoria) {
		return new LivroResponse(1L, "Dom Casmurro", "978-85-359-0277-5", 1899, null, null,
				new AutorResumo(1L, "Machado de Assis"), categoria);
	}

}
