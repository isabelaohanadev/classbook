package br.com.classbook.desktop.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import br.com.classbook.desktop.view.TelaPrincipalController;

/**
 * Ponto de entrada da interface desktop (Issue #54). Carrega a tela principal de um FXML em
 * {@code view/}; a navegação entre seções é da Issue #55. Entrega à tela principal o
 * {@code HostServices}, com que os links dos materiais (Issue #60) abrem no navegador padrão.
 */
public class AplicacaoDesktop extends Application {

	@Override
	public void start(Stage palco) throws IOException {
		FXMLLoader carregador = new FXMLLoader(
				getClass().getResource("/br/com/classbook/desktop/view/tela-principal.fxml"));
		Parent raiz = carregador.load();
		TelaPrincipalController telaPrincipal = carregador.getController();
		telaPrincipal.configurar(getHostServices());
		Scene cena = new Scene(raiz);
		cena.getStylesheets().add(getClass().getResource("/br/com/classbook/desktop/view/styles.css").toExternalForm());
		palco.setTitle("ClassBook — Biblioteca Digital");
		palco.setScene(cena);
		palco.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
