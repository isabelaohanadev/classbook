package br.com.classbook;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Monta o corpo de um {@code PATCH} nos testes sem escapar as aspas: {@code Json.de("{'nome': 'X'}")} é o
 * mesmo que {@code {"nome": "X"}}. Por isso nenhum valor de teste pode ter apóstrofo.
 */
public final class Json {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private Json() {
	}

	public static JsonNode de(String json) {
		return MAPPER.readTree(json.replace('\'', '"'));
	}

	/** O mesmo texto, para enviar como corpo de uma requisição HTTP. */
	public static String texto(String json) {
		return json.replace('\'', '"');
	}

}
