package br.com.classbook.desktop.view;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import javafx.scene.control.Label;

import br.com.classbook.desktop.api.ApiIndisponivelException;
import br.com.classbook.desktop.api.ErroDaApiException;

/**
 * Mensagens de sucesso e de erro das telas (Issue #61). O erro de uma chamada à API vira o texto que a tela
 * mostra, sem stack trace: a mensagem que a API mandou ({@code 400}, {@code 404}, {@code 409}...), a do
 * backend fora do ar ou, para qualquer outro problema, uma mensagem genérica.
 */
final class Mensagens {

	static final String ERRO_INESPERADO = "Algo deu errado ao falar com a API. Tente de novo.";

	private Mensagens() {
	}

	static String deErro(Throwable erro) {
		Throwable causa = erro;
		while ((causa instanceof CompletionException || causa instanceof ExecutionException)
				&& causa.getCause() != null) {
			causa = causa.getCause();
		}
		if (causa instanceof ErroDaApiException || causa instanceof ApiIndisponivelException) {
			return causa.getMessage();
		}
		// O detalhe fica no terminal, para quem estiver desenvolvendo; a tela mostra só a mensagem genérica.
		System.err.println("Erro inesperado na chamada à API: " + causa);
		return ERRO_INESPERADO;
	}

	static void sucesso(Label aviso, String mensagem) {
		mostrar(aviso, mensagem, "aviso-sucesso");
	}

	static void erro(Label aviso, String mensagem) {
		mostrar(aviso, mensagem, "aviso-erro");
	}

	private static void mostrar(Label aviso, String mensagem, String estilo) {
		aviso.getStyleClass().removeAll("aviso-sucesso", "aviso-erro");
		aviso.getStyleClass().add(estilo);
		aviso.setText(mensagem);
	}

}
