package br.com.classbook.desktop.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Test;

import br.com.classbook.desktop.api.ErroDaApiException;

/** As conversões dos campos, os textos com gênero e as mensagens de erro, sem precisar do JavaFX. */
class CamposEMensagensTests {

	@Test
	void textoEmBrancoViraNullEOPreenchidoPerdeOsEspacosDasPontas() {
		assertThat(Campos.texto("   ")).isNull();
		assertThat(Campos.texto((String) null)).isNull();
		assertThat(Campos.texto("  Física ")).isEqualTo("Física");
	}

	@Test
	void anoTemQueSerNumeroInteiro() {
		assertThat(Campos.inteiro("O ano", " 1899 ")).isEqualTo(1899);
		assertThat(Campos.inteiro("O ano", "")).isNull();
		assertThatThrownBy(() -> Campos.inteiro("O ano de publicação", "1899a"))
				.hasMessage("O ano de publicação deve ser um número inteiro, como 1899.");
	}

	@Test
	void dataUsaODiaMesAnoEIdaDataQueNaoExiste() {
		assertThat(Campos.data("A data", "21/06/1839")).isEqualTo(LocalDate.of(1839, 6, 21));
		assertThat(Campos.texto(LocalDate.of(2026, 3, 1))).isEqualTo("01/03/2026");
		assertThatThrownBy(() -> Campos.data("A data de nascimento", "31/02/2000"))
				.hasMessage("A data de nascimento deve ser uma data válida no formato dd/mm/aaaa.");
		assertThatThrownBy(() -> Campos.data("A data", "2000-02-01")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void celulaVaziaMostraUmTraco() {
		assertThat(Campos.ouTraco(null)).isEqualTo("—");
		assertThat(Campos.ouTraco("")).isEqualTo("—");
		assertThat(Campos.ouTraco("Brasileira")).isEqualTo("Brasileira");
	}

	@Test
	void textosConcordamComOGeneroDoRecurso() {
		Substantivo categoria = new Substantivo("categoria", true);
		Substantivo material = new Substantivo("material digital", false);

		assertThat(categoria.cadastrado()).isEqualTo("Categoria cadastrada.");
		assertThat(categoria.nenhumCadastrado()).isEqualTo("Nenhuma categoria cadastrada.");
		assertThat(categoria.tituloDoCadastro()).isEqualTo("Nova categoria");
		assertThat(material.excluido()).isEqualTo("Material digital excluído.");
		assertThat(material.atualizado()).isEqualTo("Material digital atualizado.");
		assertThat(material.perguntaDeExclusao("Lista")).isEqualTo("Excluir o material digital \"Lista\"?");
	}

	@Test
	void erroDaApiMostraAMensagemRecebidaMesmoEmbrulhado() {
		ErroDaApiException erro = new ErroDaApiException(409, "Autor 9 possui livros vinculados.", List.of());

		assertThat(Mensagens.deErro(erro)).isEqualTo("Autor 9 possui livros vinculados.");
		assertThat(Mensagens.deErro(new CompletionException(erro))).isEqualTo("Autor 9 possui livros vinculados.");
	}

	@Test
	void erroInesperadoMostraMensagemGenericaSemDetalheTecnico() {
		assertThat(Mensagens.deErro(new IllegalStateException("Cannot deserialize value of type `Long`")))
				.isEqualTo(Mensagens.ERRO_INESPERADO);
	}

}
