package br.com.classbook.desktop.view;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import br.com.classbook.desktop.modelo.AutorRequest;
import br.com.classbook.desktop.modelo.AutorResponse;

/** Controller de {@code formulario-autor.fxml} (Issue #58). */
public class FormularioAutorController implements Formulario<AutorRequest, AutorResponse> {

	@FXML
	private TextField nome;

	@FXML
	private TextField nacionalidade;

	@FXML
	private TextField dataNascimento;

	@FXML
	private TextArea biografia;

	@Override
	public void preencher(AutorResponse autor) {
		nome.setText(autor.nome());
		nacionalidade.setText(autor.nacionalidade());
		dataNascimento.setText(Campos.texto(autor.dataNascimento()));
		biografia.setText(autor.biografia());
	}

	@Override
	public List<Obrigatorio> obrigatorios() {
		return List.of(Obrigatorio.texto("Nome", nome));
	}

	@Override
	public AutorRequest montar() {
		return new AutorRequest(Campos.texto(nome.getText()), Campos.texto(nacionalidade.getText()),
				Campos.data("A data de nascimento", dataNascimento.getText()), Campos.texto(biografia.getText()));
	}

}
