package br.com.classbook.desktop.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ponto de entrada da interface desktop (Issue #54). Carrega a tela principal de um FXML em
 * {@code view/}; a navegação entre seções é da Issue #55.
 */
public class AplicacaoDesktop extends Application {

	@Override
	public void start(Stage palco) throws IOException {
		FXMLLoader carregador = new FXMLLoader(
				getClass().getResource("/br/com/classbook/desktop/view/tela-principal.fxml"));
		Parent raiz = carregador.load();
		palco.setTitle("ClassBook");
		palco.setScene(new Scene(raiz));
		palco.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
