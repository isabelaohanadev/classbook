package br.com.classbook.desktop.view;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

import br.com.classbook.desktop.api.ClienteApi;

/**
 * Controller do FXML de um formulário de cadastro e edição (Issue #58). A {@link JanelaDeFormulario} cuida
 * dos botões Salvar e Cancelar e da chamada à API; o formulário só conhece os próprios campos.
 *
 * @param <Q> o {@code Request} que o formulário monta
 * @param <R> o {@code Response} que ele recebe para editar
 */
interface Formulario<Q, R> {

	/** Preenche os campos com o registro que está sendo editado. */
	void preencher(R registro);

	/** Os campos obrigatórios: se algum estiver vazio, a janela o marca e não chama a API. */
	List<Obrigatorio> obrigatorios();

	/** Monta o {@code Request}; um valor que não dá para converter lança {@link IllegalArgumentException}. */
	Q montar();

	/**
	 * Carrega da API as listas de escolha (autores, categorias, disciplinas), em segundo plano, e seleciona
	 * as do registro editado. Só os formulários com listas sobrescrevem.
	 */
	default void carregarListas(ClienteApi api, R registro, Consumer<String> aoFalhar) {
	}

	record Obrigatorio(String rotulo, Control campo, BooleanSupplier vazio) {

		static Obrigatorio texto(String rotulo, TextInputControl campo) {
			return new Obrigatorio(rotulo, campo, () -> Campos.texto(campo.getText()) == null);
		}

		static Obrigatorio escolha(String rotulo, ComboBox<?> campo) {
			return new Obrigatorio(rotulo, campo, () -> campo.getValue() == null);
		}

	}

}
