package br.com.classbook.desktop.view;

import java.util.List;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.FormatoMaterial;
import br.com.classbook.desktop.modelo.MaterialDigitalRequest;
import br.com.classbook.desktop.modelo.MaterialDigitalResponse;

/**
 * Controller de {@code formulario-material.fxml} (Issue #58). A lista de disciplinas vem da API quando o
 * formulário abre.
 */
public class FormularioMaterialController implements Formulario<MaterialDigitalRequest, MaterialDigitalResponse> {

	@FXML
	private TextField titulo;

	@FXML
	private ComboBox<FormatoMaterial> formato;

	@FXML
	private TextField url;

	@FXML
	private ComboBox<DisciplinaResponse> disciplina;

	@FXML
	private TextField dataPublicacao;

	@FXML
	private TextArea descricao;

	@FXML
	private void initialize() {
		formato.getItems().setAll(FormatoMaterial.values());
		formato.setConverter(Campos.exibicao(FormatoMaterial::rotulo));
		disciplina.setConverter(Campos.exibicao(item -> item.codigo() + " — " + item.nome()));
	}

	@Override
	public void preencher(MaterialDigitalResponse material) {
		titulo.setText(material.titulo());
		formato.setValue(material.formato());
		url.setText(material.url());
		dataPublicacao.setText(Campos.texto(material.dataPublicacao()));
		descricao.setText(material.descricao());
	}

	@Override
	public void carregarListas(ClienteApi api, MaterialDigitalResponse material, Consumer<String> aoFalhar) {
		EmSegundoPlano.executar(() -> api.disciplinas().listar(), disciplinas -> {
			disciplina.getItems().setAll(disciplinas);
			disciplina.setPromptText("Escolha a disciplina");
			if (material != null) {
				disciplinas.stream().filter(item -> item.id().equals(material.disciplina().id()))
						.findFirst().ifPresent(disciplina::setValue);
			}
		}, falha -> {
			disciplina.setPromptText("");
			aoFalhar.accept("Não foi possível carregar as disciplinas. " + Mensagens.deErro(falha));
		});
	}

	@Override
	public List<Obrigatorio> obrigatorios() {
		return List.of(Obrigatorio.texto("Título", titulo), Obrigatorio.escolha("Formato", formato),
				Obrigatorio.texto("URL", url), Obrigatorio.escolha("Disciplina", disciplina));
	}

	@Override
	public MaterialDigitalRequest montar() {
		return new MaterialDigitalRequest(Campos.texto(titulo.getText()), formato.getValue(),
				Campos.texto(url.getText()), Campos.texto(descricao.getText()),
				Campos.data("A data de publicação", dataPublicacao.getText()), disciplina.getValue().id());
	}

}
