package br.com.classbook.desktop.view;

import static br.com.classbook.desktop.view.Fx.esperarAte;
import static br.com.classbook.desktop.view.Fx.naThread;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicReference;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.classbook.desktop.api.ApiFalsa;
import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.DisciplinaRequest;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.SerieEscolar;

/** A {@code tela-de-cadastro.fxml} com a configuração de Disciplinas, contra a {@link ApiFalsa}. */
class TelaDeCadastroTests {

	private static final String DUAS_DISCIPLINAS = """
			[{"id": 4, "nome": "Matemática", "codigo": "MAT-1", "descricao": null, "serieEscolar": "PRIMEIRA_SERIE"},
			 {"id": 5, "nome": "Física", "codigo": "FIS-2", "descricao": null, "serieEscolar": "SEGUNDA_SERIE"}]""";

	private ApiFalsa api;
	private Parent tela;
	private TelaDeCadastroController<DisciplinaRequest, DisciplinaResponse> controlador;

	@BeforeAll
	static void iniciarJavaFx() throws InterruptedException {
		Fx.iniciar();
	}

	@BeforeEach
	void subirApiFalsa() throws IOException {
		api = new ApiFalsa();
	}

	@AfterEach
	void derrubarApiFalsa() {
		api.close();
	}

	@Test
	void listaOsRegistrosDaApiComAsColunasDoRecurso() throws Exception {
		api.responder("GET /api/disciplinas", 200, DUAS_DISCIPLINAS);
		abrir(new ClienteApi(api.url()));

		esperarAte("a lista chegar", () -> tabela().getItems().size() == 2);
		assertThat(naThread(() -> tabela().getColumns().stream().map(coluna -> coluna.getText()).toList()))
				.containsExactly("Código", "Nome", "Série");
		assertThat(naThread(() -> tabela().getColumns().get(2).getCellObservableValue(0).getValue()))
				.isEqualTo("1ª série");
		assertThat(naThread(() -> texto("#titulo"))).isEqualTo("Disciplinas");
	}

	@Test
	void listaVaziaMostraAMensagemDoRecurso() throws Exception {
		api.responder("GET /api/disciplinas", 200, "[]");
		abrir(new ClienteApi(api.url()));

		esperarAte("a lista vazia chegar", () -> placeholder().getText().startsWith("Nenhuma"));
		assertThat(naThread(() -> placeholder().getText())).isEqualTo("Nenhuma disciplina cadastrada.");
	}

	@Test
	void backendForaDoArMostraUmaMensagemAmigavelSemStackTrace() throws Exception {
		int portaSemServidor;
		try (ServerSocket socket = new ServerSocket(0)) {
			portaSemServidor = socket.getLocalPort();
		}
		abrir(new ClienteApi("http://127.0.0.1:" + portaSemServidor + "/api"));

		esperarAte("o erro aparecer", () -> placeholder().getText().startsWith("Não foi possível"));
		assertThat(naThread(() -> placeholder().getText())).isEqualTo("Não foi possível falar com a API em "
				+ "http://127.0.0.1:" + portaSemServidor + "/api. Confira se o backend está rodando.");
		assertThat(naThread(() -> placeholder().getStyleClass())).contains("mensagem-erro");
	}

	@Test
	void exclusaoConfirmadaChamaDeleteMostraSucessoERecarrega() throws Exception {
		api.responder("GET /api/disciplinas", 200, DUAS_DISCIPLINAS);
		api.responder("DELETE /api/disciplinas/4", 204, "");
		abrir(new ClienteApi(api.url()));
		esperarAte("a lista chegar", () -> tabela().getItems().size() == 2);
		AtomicReference<String> pergunta = new AtomicReference<>();

		naThread(() -> {
			controlador.confirmar = texto -> {
				pergunta.set(texto);
				return true;
			};
			tabela().getSelectionModel().select(0);
			controlador.excluir();
			return null;
		});
		esperarAte("o aviso de sucesso", () -> texto("#aviso").equals("Disciplina excluída."));

		assertThat(pergunta).hasValue("Excluir a disciplina \"Matemática\"?");
		assertThat(api.alteracoes()).extracting(ApiFalsa.Requisicao::rota).containsExactly("DELETE /api/disciplinas/4");
		assertThat(naThread(() -> aviso().getStyleClass())).contains("aviso-sucesso");
		esperarAte("a lista ser recarregada",
				() -> api.recebidas().stream().filter(r -> r.rota().equals("GET /api/disciplinas")).count() == 2);
	}

	@Test
	void exclusaoNaoConfirmadaNaoChamaAApi() throws Exception {
		api.responder("GET /api/disciplinas", 200, DUAS_DISCIPLINAS);
		abrir(new ClienteApi(api.url()));
		esperarAte("a lista chegar", () -> tabela().getItems().size() == 2);

		naThread(() -> {
			controlador.confirmar = texto -> false;
			tabela().getSelectionModel().select(0);
			controlador.excluir();
			return null;
		});

		assertThat(api.alteracoes()).isEmpty();
	}

	@Test
	void erroDaApiAoExcluirApareceNoAvisoComAMensagemRecebida() throws Exception {
		api.responder("GET /api/disciplinas", 200, DUAS_DISCIPLINAS);
		api.responder("DELETE /api/disciplinas/4", 409, """
				{"horario": "2026-11-04T12:00:00Z", "status": 409, "erro": "Conflito",
				 "mensagem": "Disciplina 4 possui materiais vinculados.", "caminho": "/api/disciplinas/4",
				 "campos": []}""");
		abrir(new ClienteApi(api.url()));
		esperarAte("a lista chegar", () -> tabela().getItems().size() == 2);

		naThread(() -> {
			controlador.confirmar = texto -> true;
			tabela().getSelectionModel().select(0);
			controlador.excluir();
			return null;
		});
		esperarAte("o aviso de erro", () -> !texto("#aviso").isEmpty());

		assertThat(naThread(() -> texto("#aviso"))).isEqualTo("Disciplina 4 possui materiais vinculados.");
		assertThat(naThread(() -> aviso().getStyleClass())).contains("aviso-erro");
	}

	@Test
	void cadastroPeloBotaoNovoMostraSucessoERecarregaALista() throws Exception {
		api.responder("GET /api/disciplinas", 200, "[]");
		api.responder("POST /api/disciplinas", 201, """
				{"id": 6, "nome": "Química", "codigo": "QUI-1", "serieEscolar": "PRIMEIRA_SERIE"}""");
		abrir(new ClienteApi(api.url()));
		esperarAte("a lista chegar", () -> placeholder().getText().startsWith("Nenhuma"));

		naThread(() -> {
			controlador.novo();
			var dialogo = controlador.ultimoFormulario.dialogo().getDialogPane();
			((TextField) dialogo.lookup("#nome")).setText("Química");
			((TextField) dialogo.lookup("#codigo")).setText("qui-1");
			@SuppressWarnings("unchecked")
			ComboBox<SerieEscolar> serie = (ComboBox<SerieEscolar>) dialogo.lookup("#serieEscolar");
			serie.setValue(SerieEscolar.PRIMEIRA_SERIE);
			((Button) dialogo.lookupButton(JanelaDeFormulario.SALVAR)).fire();
			return null;
		});
		esperarAte("o aviso de sucesso", () -> texto("#aviso").equals("Disciplina cadastrada."));

		assertThat(api.alteracoes()).extracting(ApiFalsa.Requisicao::rota).containsExactly("POST /api/disciplinas");
		esperarAte("a lista ser recarregada",
				() -> api.recebidas().stream().filter(r -> r.rota().equals("GET /api/disciplinas")).count() == 2);
	}

	private void abrir(ClienteApi cliente) throws Exception {
		naThread(() -> {
			FXMLLoader carregador = new FXMLLoader(TelaDeCadastroController.class.getResource("tela-de-cadastro.fxml"));
			tela = carregador.load();
			controlador = carregador.getController();
			Stage palco = new Stage();
			palco.setScene(new Scene(tela));
			palco.show();
			controlador.configurar(Cadastro.disciplinas(), cliente);
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	private TableView<DisciplinaResponse> tabela() {
		return (TableView<DisciplinaResponse>) tela.lookup("#tabela");
	}

	private Label placeholder() {
		return (Label) tabela().getPlaceholder();
	}

	private Label aviso() {
		return (Label) tela.lookup("#aviso");
	}

	private String texto(String seletor) {
		return ((Label) tela.lookup(seletor)).getText();
	}

}
