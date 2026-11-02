package br.com.classbook.exception;

/**
 * Regra de entrada conferida no service, que depende do banco e por isso não cabe no
 * {@code @Valid} do controller: por exemplo, {@code autorId} inexistente. O
 * {@link TratadorGlobalDeExcecoes} responde {@code 400} com esta mensagem (ver
 * docs/ARQUITETURA.md#erros-e-exceções).
 */
public class DadosInvalidosException extends RuntimeException {

	public DadosInvalidosException(String mensagem) {
		super(mensagem);
	}

}
