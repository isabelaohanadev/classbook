package br.com.classbook.desktop.view;

import java.util.List;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.AutorResponse;
import br.com.classbook.desktop.modelo.CategoriaResponse;
import br.com.classbook.desktop.modelo.LivroRequest;
import br.com.classbook.desktop.modelo.LivroResponse;

/**
 * Controller de {@code formulario-livro.fxml} (Issue #58). As listas de autor e de categoria vêm da API
 * quando o formulário abre; a categoria é opcional.
 */
public class FormularioLivroController implements Formulario<LivroRequest, LivroResponse> {

	@FXML
	private TextField titulo;

	@FXML
	private TextField isbn;

	@FXML
	private ComboBox<AutorResponse> autor;

	@FXML
	private ComboBox<CategoriaResponse> categoria;

	@FXML
	private TextField anoPublicacao;

	@FXML
	private TextField editora;

	@FXML
	private TextArea descricao;

	@FXML
	private void initialize() {
		autor.setConverter(Campos.exibicao(AutorResponse::nome));
		categoria.setConverter(Campos.exibicao(CategoriaResponse::nome));
	}

	@FXML
	private void limparCategoria() {
		categoria.setValue(null);
	}

	@Override
	public void preencher(LivroResponse livro) {
		titulo.setText(livro.titulo());
		isbn.setText(livro.isbn());
		anoPublicacao.setText(Campos.texto(livro.anoPublicacao()));
		editora.setText(livro.editora());
		descricao.setText(livro.descricao());
	}

	@Override
	public void carregarListas(ClienteApi api, LivroResponse livro, Consumer<String> aoFalhar) {
		EmSegundoPlano.executar(() -> new Listas(api.autores().listar(), api.categorias().listar()), listas -> {
			autor.getItems().setAll(listas.autores());
			autor.setPromptText("Escolha o autor");
			categoria.getItems().setAll(listas.categorias());
			if (livro != null) {
				listas.autores().stream().filter(item -> item.id().equals(livro.autor().id()))
						.findFirst().ifPresent(autor::setValue);
				if (livro.categoria() != null) {
					listas.categorias().stream().filter(item -> item.id().equals(livro.categoria().id()))
							.findFirst().ifPresent(categoria::setValue);
				}
			}
		}, falha -> {
			autor.setPromptText("");
			aoFalhar.accept("Não foi possível carregar os autores e as categorias. " + Mensagens.deErro(falha));
		});
	}

	@Override
	public List<Obrigatorio> obrigatorios() {
		return List.of(Obrigatorio.texto("Título", titulo), Obrigatorio.texto("ISBN", isbn),
				Obrigatorio.escolha("Autor", autor));
	}

	@Override
	public LivroRequest montar() {
		CategoriaResponse escolhida = categoria.getValue();
		return new LivroRequest(Campos.texto(titulo.getText()), Campos.texto(isbn.getText()),
				Campos.inteiro("O ano de publicação", anoPublicacao.getText()), Campos.texto(descricao.getText()),
				Campos.texto(editora.getText()), autor.getValue().id(), escolhida == null ? null : escolhida.id());
	}

	private record Listas(List<AutorResponse> autores, List<CategoriaResponse> categorias) {
	}

}
