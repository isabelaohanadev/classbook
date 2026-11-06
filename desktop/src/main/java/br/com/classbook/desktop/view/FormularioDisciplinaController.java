package br.com.classbook.desktop.view;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import br.com.classbook.desktop.modelo.DisciplinaRequest;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.SerieEscolar;

/** Controller de {@code formulario-disciplina.fxml} (Issue #58). A API grava o código em maiúsculas. */
public class FormularioDisciplinaController implements Formulario<DisciplinaRequest, DisciplinaResponse> {

	@FXML
	private TextField nome;

	@FXML
	private TextField codigo;

	@FXML
	private ComboBox<SerieEscolar> serieEscolar;

	@FXML
	private TextArea descricao;

	@FXML
	private void initialize() {
		serieEscolar.getItems().setAll(SerieEscolar.values());
		serieEscolar.setConverter(Campos.exibicao(SerieEscolar::rotulo));
	}

	@Override
	public void preencher(DisciplinaResponse disciplina) {
		nome.setText(disciplina.nome());
		codigo.setText(disciplina.codigo());
		serieEscolar.setValue(disciplina.serieEscolar());
		descricao.setText(disciplina.descricao());
	}

	@Override
	public List<Obrigatorio> obrigatorios() {
		return List.of(Obrigatorio.texto("Nome", nome), Obrigatorio.texto("Código", codigo),
				Obrigatorio.escolha("Série", serieEscolar));
	}

	@Override
	public DisciplinaRequest montar() {
		return new DisciplinaRequest(Campos.texto(nome.getText()), Campos.texto(codigo.getText()),
				Campos.texto(descricao.getText()), serieEscolar.getValue());
	}

}
