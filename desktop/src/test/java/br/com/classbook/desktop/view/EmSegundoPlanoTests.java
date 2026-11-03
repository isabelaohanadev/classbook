package br.com.classbook.desktop.view;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import br.com.classbook.desktop.api.ErroDaApiException;

/**
 * As threads do JavaFX são trocadas por duas threads com nome ({@code segundo-plano} e {@code interface}),
 * para conferir onde cada parte roda sem abrir janela.
 */
class EmSegundoPlanoTests {

	private final ExecutorService segundoPlano =
			Executors.newSingleThreadExecutor(tarefa -> new Thread(tarefa, "segundo-plano"));
	private final ExecutorService threadDaInterface =
			Executors.newSingleThreadExecutor(tarefa -> new Thread(tarefa, "interface"));

	@AfterEach
	void encerrarThreads() {
		segundoPlano.shutdownNow();
		threadDaInterface.shutdownNow();
	}

	@Test
	void chamadaRodaForaDaThreadDaInterfaceEOResultadoVoltaParaEla() throws Exception {
		AtomicReference<String> threadDaChamada = new AtomicReference<>();
		CompletableFuture<String> entregue = new CompletableFuture<>();

		EmSegundoPlano.executar(() -> {
			threadDaChamada.set(Thread.currentThread().getName());
			return "3 autores";
		}, resultado -> entregue.complete(resultado + " em " + Thread.currentThread().getName()),
				entregue::completeExceptionally, segundoPlano, threadDaInterface);

		assertThat(entregue.get(5, SECONDS)).isEqualTo("3 autores em interface");
		assertThat(threadDaChamada.get()).isEqualTo("segundo-plano");
	}

	@Test
	void erroDaChamadaVaiParaAoFalharNaThreadDaInterface() throws Exception {
		CompletableFuture<String> entregue = new CompletableFuture<>();

		EmSegundoPlano.<String>executar(() -> {
			throw new ErroDaApiException(404, "Autor 9 não encontrado.", List.of());
		}, resultado -> entregue.complete("não devia concluir"),
				erro -> entregue.complete(erro.getMessage() + " em " + Thread.currentThread().getName()),
				segundoPlano, threadDaInterface);

		assertThat(entregue.get(5, SECONDS)).isEqualTo("Autor 9 não encontrado. em interface");
	}

}
