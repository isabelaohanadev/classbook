package br.com.classbook.desktop.view;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.MaterialDigitalResponse;

/**
 * Controller de {@code detalhes-livro.fxml} (Issue #60): todos os dados do livro selecionado no catálogo
 * ({@link #preencher(LivroResponse)}), mais uma área de materiais digitais por disciplina
 * ({@link #carregar(ClienteApi)}). Pela modelagem, materiais se ligam a disciplinas, não a livros, então
 * essa área não é filtrada pelo livro — é um jeito de navegar os materiais a partir da mesma tela.
 *
 * <p>Cada material tem um link que abre no navegador padrão (Issue #60); um link ausente ou inválido só
 * mostra um aviso, sem derrubar a aplicação.
 */
public class DetalhesLivroController {

	private static final String TRACO = "—";

	@FXML
	private Label titulo;

	@FXML
	private Label autor;

	@FXML
	private Label categoria;

	@FXML
	private Label isbn;

	@FXML
	private Label ano;

	@FXML
	private Label editora;

	@FXML
	private Label descricao;

	@FXML
	private ComboBox<DisciplinaResponse> comboDisciplina;

	@FXML
	private ListView<MaterialDigitalResponse> listaDeMateriais;

	@FXML
	private Label mensagemSemMateriais;

	@FXML
	private Label avisoDeLink;

	private List<MaterialDigitalResponse> todosOsMateriais = List.of();

	/** Abre o link no navegador padrão; os testes trocam para não abrir de verdade. */
	Consumer<URI> abrirNoNavegador = DetalhesLivroController::abrirDeVerdade;

	@FXML
	private void initialize() {
		comboDisciplina.setConverter(Campos.exibicao(DisciplinaResponse::nome));
		comboDisciplina.valueProperty().addListener((observavel, anterior, atual) -> filtrarMateriais());
		listaDeMateriais.setCellFactory(lista -> new CelulaDeMaterial());
	}

	void preencher(LivroResponse livro) {
		titulo.setText(livro.titulo());
		autor.setText(livro.autor().nome());
		categoria.setText(livro.categoria() == null ? TRACO : livro.categoria().nome());
		isbn.setText(livro.isbn());
		ano.setText(CatalogoLivrosController.ano(livro));
		editora.setText(Campos.ouTraco(livro.editora()));
		descricao.setText(Campos.ouTraco(livro.descricao()));
	}

	void carregar(ClienteApi api) {
		EmSegundoPlano.executar(() -> new Listas(api.disciplinas().listar(), api.materiais().listar()), listas -> {
			comboDisciplina.getItems().setAll(listas.disciplinas());
			todosOsMateriais = listas.materiais();
			filtrarMateriais();
		}, falha -> {
			todosOsMateriais = List.of();
			listaDeMateriais.getItems().clear();
			mensagemSemMateriais.getStyleClass().removeAll("mensagem-vazia", "mensagem-erro");
			mensagemSemMateriais.getStyleClass().add("mensagem-erro");
			mensagemSemMateriais.setText("Não foi possível carregar os materiais digitais. " + Mensagens.deErro(falha));
		});
	}

	private void filtrarMateriais() {
		DisciplinaResponse escolhida = comboDisciplina.getValue();
		List<MaterialDigitalResponse> filtrados = escolhida == null ? todosOsMateriais
				: todosOsMateriais.stream().filter(material -> material.disciplina().id().equals(escolhida.id()))
						.toList();
		listaDeMateriais.getItems().setAll(filtrados);
		mensagemSemMateriais.setText(escolhida == null ? "Nenhum material digital cadastrado."
				: "Nenhum material digital nessa disciplina.");
	}

	/** Package-private para os testes chamarem direto, sem precisar simular o clique no link da lista. */
	void abrirLink(MaterialDigitalResponse material) {
		avisoDeLink.setText("");
		String url = material == null ? null : material.url();
		if (url == null || url.isBlank()) {
			avisoDeLink.setText("Este material não tem um link.");
			return;
		}
		URI uri;
		try {
			uri = new URI(url.strip());
		} catch (URISyntaxException invalido) {
			avisoDeLink.setText("Link inválido: " + url);
			return;
		}
		try {
			abrirNoNavegador.accept(uri);
		} catch (RuntimeException falhaAoAbrir) {
			avisoDeLink.setText("Não foi possível abrir o link: " + url);
		}
	}

	private static void abrirDeVerdade(URI uri) {
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException | UnsupportedOperationException erro) {
			throw new IllegalStateException(erro);
		}
	}

	/** Uma linha da lista de materiais: título, formato e disciplina como um link clicável. */
	private final class CelulaDeMaterial extends ListCell<MaterialDigitalResponse> {

		private final Hyperlink link = new Hyperlink();

		private CelulaDeMaterial() {
			link.setOnAction(evento -> abrirLink(getItem()));
		}

		@Override
		protected void updateItem(MaterialDigitalResponse material, boolean vazio) {
			super.updateItem(material, vazio);
			if (vazio || material == null) {
				setGraphic(null);
			} else {
				link.setText(material.titulo() + " — " + material.formato().rotulo() + " ("
						+ material.disciplina().codigo() + ")");
				setGraphic(link);
			}
		}

	}

	private record Listas(List<DisciplinaResponse> disciplinas, List<MaterialDigitalResponse> materiais) {
	}

}
