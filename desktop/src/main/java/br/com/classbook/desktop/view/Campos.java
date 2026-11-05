package br.com.classbook.desktop.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.function.Function;

import javafx.util.StringConverter;

/**
 * Converte o texto dos campos dos formulários para os tipos dos {@code Request} e de volta (Issue #58). Um
 * campo em branco vira {@code null}; um valor que não dá para converter lança
 * {@link IllegalArgumentException} com a mensagem que o formulário mostra.
 */
final class Campos {

	static final DateTimeFormatter FORMATO_DE_DATA =
			DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

	private Campos() {
	}

	static String texto(String valor) {
		return valor == null || valor.isBlank() ? null : valor.strip();
	}

	static Integer inteiro(String rotulo, String valor) {
		String texto = texto(valor);
		if (texto == null) {
			return null;
		}
		try {
			return Integer.valueOf(texto);
		} catch (NumberFormatException erro) {
			throw new IllegalArgumentException(rotulo + " deve ser um número inteiro, como 1899.");
		}
	}

	static LocalDate data(String rotulo, String valor) {
		String texto = texto(valor);
		if (texto == null) {
			return null;
		}
		try {
			return LocalDate.parse(texto, FORMATO_DE_DATA);
		} catch (DateTimeParseException erro) {
			throw new IllegalArgumentException(rotulo + " deve ser uma data válida no formato dd/mm/aaaa.");
		}
	}

	static String texto(LocalDate data) {
		return data == null ? "" : FORMATO_DE_DATA.format(data);
	}

	static String texto(Integer numero) {
		return numero == null ? "" : numero.toString();
	}

	/** O que uma célula da tabela mostra: o valor, ou "—" quando ele não foi preenchido. */
	static String ouTraco(String valor) {
		return valor == null || valor.isBlank() ? "—" : valor;
	}

	/** Como um item aparece numa lista de escolha ({@code ComboBox}); as listas não são editáveis. */
	static <T> StringConverter<T> exibicao(Function<T, String> texto) {
		return new StringConverter<>() {

			@Override
			public String toString(T item) {
				return item == null ? "" : texto.apply(item);
			}

			@Override
			public T fromString(String texto) {
				throw new UnsupportedOperationException("A lista de escolha não é editável.");
			}

		};
	}

}
