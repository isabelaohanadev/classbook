package br.com.classbook;

import java.util.Locale;
import java.util.UUID;

/**
 * Deixa únicos, a cada execução da suíte, os valores que o banco exige únicos: o nome da categoria e o
 * código da disciplina. Assim nenhum teste depende do que já existe no banco de teste, e uma
 * "Literatura" ou um "MAT-1" cadastrados à mão não geram conflito.
 *
 * <p>Todos os valores de uma execução recebem o mesmo sufixo, então a ordem alfabética entre eles e a
 * comparação sem diferenciar maiúsculas de minúsculas continuam valendo nos testes.
 */
public final class Unico {

	private static final String SUFIXO = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);

	private Unico() {
	}

	/** Nome de categoria único: {@code "Literatura"} vira {@code "Literatura 3F9A1C"}. */
	public static String nome(String nome) {
		return nome + " " + SUFIXO;
	}

	/** Código de disciplina único: {@code "MAT-1"} vira {@code "MAT-1-3F9A1C"}, com até 13 caracteres. */
	public static String codigo(String codigo) {
		return codigo + "-" + SUFIXO;
	}

}
