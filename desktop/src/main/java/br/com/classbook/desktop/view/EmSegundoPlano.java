package br.com.classbook.desktop.view;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * Roda uma chamada à API fora da thread do JavaFX, para a janela não travar enquanto a resposta não chega,
 * e entrega o resultado (ou o erro) de volta na thread do JavaFX, onde a tela pode ser atualizada (Issue #59).
 *
 * <pre>{@code
 * EmSegundoPlano.executar(() -> api.autores().listar(), tabela.getItems()::setAll, this::mostrarErro);
 * }</pre>
 */
public final class EmSegundoPlano {

	// Threads daemon: uma chamada ainda em andamento não impede a aplicação de fechar.
	private static final ExecutorService CHAMADAS = Executors.newCachedThreadPool(tarefa -> {
		Thread thread = new Thread(tarefa, "classbook-api");
		thread.setDaemon(true);
		return thread;
	});

	private EmSegundoPlano() {
	}

	public static <T> void executar(Callable<T> chamada, Consumer<T> aoConcluir, Consumer<Exception> aoFalhar) {
		executar(chamada, aoConcluir, aoFalhar, CHAMADAS, Platform::runLater);
	}

	/** Versão com as threads explícitas, para os testes não dependerem do JavaFX. */
	static <T> void executar(Callable<T> chamada, Consumer<T> aoConcluir, Consumer<Exception> aoFalhar,
			Executor segundoPlano, Executor threadDaInterface) {
		segundoPlano.execute(() -> {
			try {
				T resultado = chamada.call();
				threadDaInterface.execute(() -> aoConcluir.accept(resultado));
			} catch (Exception erro) {
				threadDaInterface.execute(() -> aoFalhar.accept(erro));
			}
		});
	}

}
