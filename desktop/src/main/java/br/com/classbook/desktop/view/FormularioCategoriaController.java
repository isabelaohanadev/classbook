package br.com.classbook.desktop.view;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import br.com.classbook.desktop.modelo.CategoriaRequest;
import br.com.classbook.desktop.modelo.CategoriaResponse;
import br.com.classbook.desktop.modelo.TipoCategoria;

/** Controller de {@code formulario-categoria.fxml} (Issue #58). */
public class FormularioCategoriaController implements Formulario<CategoriaRequest, CategoriaResponse> {

	@FXML
	private TextField nome;

	@FXML
	private ComboBox<TipoCategoria> tipo;

	@FXML
	private TextField faixaEtaria;

	@FXML
	private TextArea descricao;

	@FXML
	private void initialize() {
		tipo.getItems().setAll(TipoCategoria.values());
		tipo.setConverter(Campos.exibicao(TipoCategoria::rotulo));
	}

	@Override
	public void preencher(CategoriaResponse categoria) {
		nome.setText(categoria.nome());
		tipo.setValue(categoria.tipo());
		faixaEtaria.setText(categoria.faixaEtaria());
		descricao.setText(categoria.descricao());
	}

	@Override
	public List<Obrigatorio> obrigatorios() {
		return List.of(Obrigatorio.texto("Nome", nome), Obrigatorio.escolha("Tipo", tipo));
	}

	@Override
	public CategoriaRequest montar() {
		return new CategoriaRequest(Campos.texto(nome.getText()), Campos.texto(descricao.getText()),
				Campos.texto(faixaEtaria.getText()), tipo.getValue());
	}

}
