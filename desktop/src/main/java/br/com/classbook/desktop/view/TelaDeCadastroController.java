package br.com.classbook.desktop.view;

import java.util.function.Predicate;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.view.Cadastro.Coluna;

/**
 * Controller de {@code tela-de-cadastro.fxml}, usada por Autores, Categorias, Disciplinas e Materiais
 * (Issues #58 e #61): lista os registros vindos da API e abre a {@link JanelaDeFormulario} para cadastrar
 * ou editar. Depois de cada operação, mostra o resultado no aviso do cabeçalho e recarrega a lista.
 */
public class TelaDeCadastroController<Q, R> {

	@FXML
	private Label titulo;

	@FXML
	private Label aviso;

	@FXML
	private Button editar;

	@FXML
	private Button excluir;

	@FXML
	private TableView<R> tabela;

	@FXML
	private Label mensagemVazia;

	private Cadastro<Q, R> cadastro;
	private ClienteApi api;

	/** Pergunta antes de excluir; os testes trocam para não abrir o diálogo. */
	Predicate<String> confirmar = TelaDeCadastroController::confirmarComAlerta;

	/** O último formulário aberto, para os testes. */
	JanelaDeFormulario<Q, R> ultimoFormulario;

	void configurar(Cadastro<Q, R> cadastro, ClienteApi api) {
		this.cadastro = cadastro;
		this.api = api;
		titulo.setText(cadastro.titulo());
		for (Coluna<R> coluna : cadastro.colunas()) {
			TableColumn<R, String> colunaDaTabela = new TableColumn<>(coluna.titulo());
			colunaDaTabela.setCellValueFactory(
					linha -> new ReadOnlyStringWrapper(Campos.ouTraco(coluna.valor().apply(linha.getValue()))));
			tabela.getColumns().add(colunaDaTabela);
		}
		editar.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
		excluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
		tabela.setOnMouseClicked(clique -> {
			if (clique.getClickCount() == 2) {
				editar();
			}
		});
		carregar();
	}

	@FXML
	void novo() {
		abrirFormulario(null);
	}

	@FXML
	void editar() {
		R selecionado = tabela.getSelectionModel().getSelectedItem();
		if (selecionado != null) {
			abrirFormulario(selecionado);
		}
	}

	@FXML
	void excluir() {
		R selecionado = tabela.getSelectionModel().getSelectedItem();
		if (selecionado == null
				|| !confirmar.test(cadastro.nome().perguntaDeExclusao(cadastro.descricao().apply(selecionado)))) {
			return;
		}
		long id = cadastro.id().apply(selecionado);
		EmSegundoPlano.executar(() -> {
			cadastro.recurso().apply(api).excluir(id);
			return id;
		}, excluido -> {
			Mensagens.sucesso(aviso, cadastro.nome().excluido());
			carregar();
		}, falha -> Mensagens.erro(aviso, Mensagens.deErro(falha)));
	}

	@FXML
	void atualizar() {
		aviso.setText("");
		carregar();
	}

	private void carregar() {
		mensagemVazia.setText("Carregando...");
		EmSegundoPlano.executar(() -> cadastro.recurso().apply(api).listar(), registros -> {
			tabela.getItems().setAll(registros);
			mensagemVazia.getStyleClass().setAll("label", "mensagem-vazia");
			mensagemVazia.setText(cadastro.nome().nenhumCadastrado());
		}, falha -> {
			tabela.getItems().clear();
			mensagemVazia.getStyleClass().setAll("label", "mensagem-erro");
			mensagemVazia.setText(Mensagens.deErro(falha));
		});
	}

	private void abrirFormulario(R registro) {
		ultimoFormulario = new JanelaDeFormulario<>(cadastro, api, registro, salvo -> {
			Mensagens.sucesso(aviso, registro == null ? cadastro.nome().cadastrado() : cadastro.nome().atualizado());
			carregar();
		});
		ultimoFormulario.abrir(tabela.getScene() == null ? null : tabela.getScene().getWindow());
	}

	private static boolean confirmarComAlerta(String pergunta) {
		Alert alerta = new Alert(AlertType.CONFIRMATION, pergunta, ButtonType.YES, ButtonType.NO);
		alerta.setHeaderText(null);
		return alerta.showAndWait().filter(ButtonType.YES::equals).isPresent();
	}

}
