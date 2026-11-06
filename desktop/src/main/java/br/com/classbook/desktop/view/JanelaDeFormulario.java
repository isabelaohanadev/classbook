package br.com.classbook.desktop.view;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.api.Recurso;
import br.com.classbook.desktop.view.Formulario.Obrigatorio;

/**
 * Diálogo de cadastro ou edição de um recurso (Issues #58 e #61), com o formulário do FXML do
 * {@link Cadastro} e os botões Salvar e Cancelar.
 *
 * <ul>
 * <li>Salvar confere os campos obrigatórios e só então chama a API, em segundo plano. Se a API recusar, a
 * mensagem dela aparece no próprio diálogo, que continua aberto com o que foi digitado.</li>
 * <li>Cancelar (ou fechar a janela) descarta as alterações sem chamar a API.</li>
 * </ul>
 */
final class JanelaDeFormulario<Q, R> {

	static final ButtonType SALVAR = new ButtonType("Salvar", ButtonData.OK_DONE);
	private static final String CAMPO_VAZIO = "campo-obrigatorio-vazio";

	private final Cadastro<Q, R> cadastro;
	private final ClienteApi api;
	private final R registro;
	private final Consumer<R> aoSalvar;
	private final Formulario<Q, R> formulario;
	private final Dialog<R> dialogo = new Dialog<>();
	private final Label erro = new Label();
	private final Button botaoSalvar;

	/**
	 * @param registro o registro a editar, ou {@code null} para um cadastro novo
	 * @param aoSalvar recebe o que a API devolveu, depois que o diálogo fecha
	 */
	JanelaDeFormulario(Cadastro<Q, R> cadastro, ClienteApi api, R registro, Consumer<R> aoSalvar) {
		this.cadastro = cadastro;
		this.api = api;
		this.registro = registro;
		this.aoSalvar = aoSalvar;

		FXMLLoader carregador = new FXMLLoader(JanelaDeFormulario.class.getResource(cadastro.formulario()));
		Parent campos;
		try {
			campos = carregador.load();
		} catch (IOException falha) {
			throw new UncheckedIOException(falha);
		}
		formulario = carregador.getController();

		erro.setWrapText(true);
		erro.setMaxWidth(460);
		erro.getStyleClass().add("aviso-erro");
		Label legenda = new Label("* campo obrigatório");
		legenda.getStyleClass().add("legenda");

		dialogo.setTitle(registro == null ? cadastro.nome().tituloDoCadastro() : cadastro.nome().tituloDaEdicao());
		dialogo.getDialogPane().setContent(new VBox(10, campos, legenda, erro));
		dialogo.getDialogPane().getStyleClass().add("formulario");
		dialogo.getDialogPane().getStylesheets()
				.add(JanelaDeFormulario.class.getResource("styles.css").toExternalForm());
		dialogo.getDialogPane().getButtonTypes().addAll(SALVAR, ButtonType.CANCEL);
		dialogo.setResultConverter(botao -> null);
		botaoSalvar = (Button) dialogo.getDialogPane().lookupButton(SALVAR);
		// Sem isto o diálogo fecharia ao clicar: ele só fecha quando a API confirma o cadastro ou a edição.
		botaoSalvar.addEventFilter(ActionEvent.ACTION, evento -> {
			evento.consume();
			salvar();
		});

		if (registro != null) {
			formulario.preencher(registro);
		}
		formulario.carregarListas(api, registro, this::mostrarErro);
	}

	void abrir(Window dono) {
		if (dono != null) {
			dialogo.initOwner(dono);
		}
		dialogo.show();
	}

	Dialog<R> dialogo() {
		return dialogo;
	}

	String mensagemDeErro() {
		return erro.getText();
	}

	private void salvar() {
		erro.setText("");
		List<String> vazios = new ArrayList<>();
		for (Obrigatorio obrigatorio : formulario.obrigatorios()) {
			boolean vazio = obrigatorio.vazio().getAsBoolean();
			obrigatorio.campo().getStyleClass().remove(CAMPO_VAZIO);
			if (vazio) {
				obrigatorio.campo().getStyleClass().add(CAMPO_VAZIO);
				vazios.add(obrigatorio.rotulo());
			}
		}
		if (!vazios.isEmpty()) {
			mostrarErro("Preencha os campos obrigatórios: " + String.join(", ", vazios) + ".");
			return;
		}
		Q request;
		try {
			request = formulario.montar();
		} catch (IllegalArgumentException invalido) {
			mostrarErro(invalido.getMessage());
			return;
		}

		botaoSalvar.setDisable(true);
		Recurso<Q, R> recurso = cadastro.recurso().apply(api);
		EmSegundoPlano.executar(
				() -> registro == null
						? recurso.cadastrar(request)
						: recurso.editar(cadastro.id().apply(registro), request),
				salvo -> {
					dialogo.setResult(salvo);
					dialogo.close();
					aoSalvar.accept(salvo);
				},
				falha -> {
					botaoSalvar.setDisable(false);
					mostrarErro(Mensagens.deErro(falha));
				});
	}

	private void mostrarErro(String mensagem) {
		erro.setText(mensagem);
	}

}
