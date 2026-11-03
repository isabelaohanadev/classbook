package br.com.classbook.desktop.api;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A API respondeu com um código de erro ({@code 4xx} ou {@code 5xx}). A mensagem é a que veio no corpo
 * {@code ErroResponse} do backend, seguida dos campos inválidos, quando houver, e pode ser mostrada como
 * está na interface.
 */
public class ErroDaApiException extends RuntimeException {

	private final int status;
	private final String mensagem;
	private final List<CampoInvalido> campos;

	public ErroDaApiException(int status, String mensagem, List<CampoInvalido> campos) {
		super(mensagemCompleta(mensagem, campos));
		this.status = status;
		this.mensagem = mensagem;
		this.campos = List.copyOf(campos);
	}

	public int getStatus() {
		return status;
	}

	/** A mensagem principal, sem a lista de campos. */
	public String getMensagem() {
		return mensagem;
	}

	public List<CampoInvalido> getCampos() {
		return campos;
	}

	private static String mensagemCompleta(String mensagem, List<CampoInvalido> campos) {
		if (campos.isEmpty()) {
			return mensagem;
		}
		return mensagem + " " + campos.stream()
				.map(campo -> campo.campo() + ": " + campo.mensagem())
				.collect(Collectors.joining("; ")) + ".";
	}

	/** Um item de {@code campos} no corpo de erro da API. */
	public record CampoInvalido(String campo, String mensagem) {
	}

}
