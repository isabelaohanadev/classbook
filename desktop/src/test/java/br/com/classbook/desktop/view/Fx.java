package br.com.classbook.desktop.view;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;

/**
 * Sobe o JavaFX uma vez para os testes das telas, sem janela (Monocle Headless, configurado no
 * {@code pom.xml}), e roda as ações na thread do JavaFX, como faria um clique.
 */
final class Fx {

	private static boolean iniciado;

	private Fx() {
	}

	static synchronized void iniciar() throws InterruptedException {
		if (iniciado) {
			return;
		}
		CountDownLatch pronto = new CountDownLatch(1);
		Platform.startup(pronto::countDown);
		pronto.await(20, SECONDS);
		// Fechar a última janela de um teste não pode encerrar o JavaFX dos testes seguintes.
		Platform.setImplicitExit(false);
		iniciado = true;
	}

	static <T> T naThread(Callable<T> acao) throws Exception {
		CompletableFuture<T> resultado = new CompletableFuture<>();
		Platform.runLater(() -> {
			try {
				resultado.complete(acao.call());
			} catch (Throwable falha) {
				resultado.completeExceptionally(falha);
			}
		});
		return resultado.get(10, SECONDS);
	}

	/** Espera a resposta da API chegar à tela: as chamadas rodam em segundo plano. */
	static void esperarAte(String descricao, Callable<Boolean> condicao) throws Exception {
		long limite = System.nanoTime() + SECONDS.toNanos(10);
		while (!naThread(condicao)) {
			if (System.nanoTime() > limite) {
				throw new AssertionError("Tempo esgotado esperando: " + descricao);
			}
			Thread.sleep(50);
		}
	}

}
