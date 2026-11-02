package br.com.classbook.desktop.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller de {@code tela-principal.fxml} (Issue #55). Cada botão do menu troca o texto do
 * conteúdo; as telas de verdade entram nas Issues #56 a #61. Não faz nenhuma chamada de rede: a
 * navegação funciona com o backend fora do ar.
 */
public class TelaPrincipalController {

	@FXML
	private Label conteudo;

	@FXML
	private void mostrarCatalogo() {
		mostrar("Catálogo de livros");
	}

	@FXML
	private void mostrarAutores() {
		mostrar("Autores");
	}

	@FXML
	private void mostrarCategorias() {
		mostrar("Categorias");
	}

	@FXML
	private void mostrarDisciplinas() {
		mostrar("Disciplinas");
	}

	@FXML
	private void mostrarMateriais() {
		mostrar("Materiais digitais");
	}

	private void mostrar(String secao) {
		conteudo.setText(secao + " (em construção)");
	}

}
