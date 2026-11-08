package br.com.classbook.service;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;

import br.com.classbook.exception.DadosInvalidosException;

/**
 * Aplica o corpo de um {@code PATCH} (JSON Merge Patch, RFC 7396) sobre o estado atual de um registro,
 * devolvendo o {@code Request} completo que o {@code PUT} receberia. Assim o {@code PATCH} e o
 * {@code PUT} seguem as mesmas regras: a normalização do construtor do {@code Request}, o Bean
 * Validation e as conferências do service (duplicidade, referências) valem para os dois.
 *
 * <p>Campo ausente no corpo mantém o valor atual; campo com {@code null} limpa o valor (e um campo
 * obrigatório limpo é um {@code 400}, como no {@code PUT}); campo desconhecido é ignorado, como no
 * {@code POST} e no {@code PUT}.
 */
@Component
public class MescladorDePatch {

	private final ObjectMapper objectMapper;

	private final Validator validator;

	public MescladorDePatch(ObjectMapper objectMapper, Validator validator) {
		this.objectMapper = objectMapper;
		this.validator = validator;
	}

	/**
	 * @param atual o {@code Request} montado a partir do registro como está hoje
	 * @param alteracoes o corpo do {@code PATCH}: só os campos a alterar
	 * @param tipo o tipo do {@code Request}, um record
	 * @throws DadosInvalidosException se o corpo não é um objeto JSON
	 * @throws ConstraintViolationException se o resultado da mesclagem viola alguma regra do {@code Request}
	 * @throws tools.jackson.core.JacksonException se um campo tem o tipo errado, como um texto em
	 *         {@code anoPublicacao}
	 */
	public <T extends Record> T mesclar(T atual, JsonNode alteracoes, Class<T> tipo) {
		if (alteracoes == null || !alteracoes.isObject()) {
			throw new DadosInvalidosException("O corpo do PATCH deve ser um objeto JSON com os campos a alterar.");
		}
		Set<String> camposDoRequest = Arrays.stream(tipo.getRecordComponents()).map(RecordComponent::getName)
				.collect(Collectors.toSet());
		ObjectNode mesclado = objectMapper.valueToTree(atual);
		for (Map.Entry<String, JsonNode> campo : alteracoes.properties()) {
			if (camposDoRequest.contains(campo.getKey())) {
				mesclado.set(campo.getKey(), campo.getValue());
			}
		}
		T resultado = objectMapper.treeToValue(mesclado, tipo);
		Set<ConstraintViolation<T>> violacoes = validator.validate(resultado);
		if (!violacoes.isEmpty()) {
			throw new ConstraintViolationException(violacoes);
		}
		return resultado;
	}

}
