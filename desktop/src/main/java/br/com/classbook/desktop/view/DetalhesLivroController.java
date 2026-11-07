package br.com.classbook.desktop.view;

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
 * mostra um aviso, sem derrubar a aplicação. Quem abre o link é o {@link #abrirNoNavegador}, que a
 * {@code AplicacaoDesktop} liga ao {@code HostServices} do JavaFX. Como o {@code HostServices} não avisa
 * quando falha, o controller só entrega a ele um link {@code http} ou {@code https} completo (o mesmo que a
 * API aceita no cadastro do material).
 */
public class DetalhesLivroController {

	private static final String TRACO = "—";

	/**
	 * O que vale enquanto ninguém configurou um navegador: falha de forma visível (o aviso de "não foi
	 * possível abrir o link") em vez de deixar o clique no link sem efeito algum.
	 */
	static final Consumer<URI> SEM_NAVEGADOR = uri -> {
		throw new IllegalStateException("Nenhum navegador foi configurado.");
	};

	@FXML
	private Label titulo;

	@FXML
	private Label autor;

	@FXML
	private Label categoria;

	@FXML
	private Label disciplina;

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

	/**
	 * Abre o link no navegador padrão. Quem monta a tela troca pelo {@code HostServices}; os testes trocam
	 * para não abrir de verdade.
	 */
	Consumer<URI> abrirNoNavegador = SEM_NAVEGADOR;

	@FXML
	private void initialize() {
		// Código e nome, como no formulário de material: a API só exige que o código seja único, então duas
		// disciplinas podem ter o mesmo nome.
		comboDisciplina.setConverter(Campos.exibicao(item -> item.codigo() + " — " + item.nome()));
		comboDisciplina.valueProperty().addListener((observavel, anterior, atual) -> filtrarMateriais());
		listaDeMateriais.setCellFactory(lista -> new CelulaDeMaterial());
	}

	void preencher(LivroResponse livro) {
		titulo.setText(livro.titulo());
		autor.setText(livro.autor().nome());
		categoria.setText(livro.categoria() == null ? TRACO : livro.categoria().nome());
		disciplina.setText(livro.disciplina() == null ? TRACO
				: livro.disciplina().codigo() + " — " + livro.disciplina().nome());
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
		if (!ehLinkWeb(uri)) {
			avisoDeLink.setText("Link inválido: " + url);
			return;
		}
		try {
			abrirNoNavegador.accept(uri);
		} catch (RuntimeException falhaAoAbrir) {
			avisoDeLink.setText("Não foi possível abrir o link: " + url);
		}
	}

	/**
	 * Só {@code http} e {@code https}, com endereço: "www.exemplo.com" (sem o esquema) é uma URI de sintaxe
	 * válida, mas o sistema não saberia abri-la, e esquemas como {@code file:} não são links de material.
	 */
	private static boolean ehLinkWeb(URI uri) {
		String esquema = uri.getScheme();
		boolean web = "http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema);
		return web && uri.getAuthority() != null;
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
