package br.com.classbook.exception;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.classbook.exception.ErroResponse.CampoInvalido;

/**
 * Tratador global (Issue #43): transforma as exceções de domínio e as do próprio Spring em
 * {@link ErroResponse}, com os códigos de docs/ARQUITETURA.md#erros-e-exceções. Os controllers não
 * tratam erros; tudo o que sai deles com exceção passa por aqui.
 */
@RestControllerAdvice
public class TratadorGlobalDeExcecoes {

	private static final Logger log = LoggerFactory.getLogger(TratadorGlobalDeExcecoes.class);

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> recursoNaoEncontrado(RecursoNaoEncontradoException ex,
			HttpServletRequest requisicao) {
		return responder(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(), requisicao);
	}

	@ExceptionHandler(DadosInvalidosException.class)
	public ResponseEntity<ErroResponse> dadosInvalidos(DadosInvalidosException ex, HttpServletRequest requisicao) {
		return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of(), requisicao);
	}

	@ExceptionHandler(ConflitoException.class)
	public ResponseEntity<ErroResponse> conflito(ConflitoException ex, HttpServletRequest requisicao) {
		return responder(HttpStatus.CONFLICT, ex.getMessage(), List.of(), requisicao);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> campoInvalido(MethodArgumentNotValidException ex,
			HttpServletRequest requisicao) {
		List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
				.map(erro -> new CampoInvalido(erro.getField(), erro.getDefaultMessage()))
				.sorted(Comparator.comparing(CampoInvalido::campo).thenComparing(CampoInvalido::mensagem))
				.toList();
		return responder(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição.", campos, requisicao);
	}

	/** JSON malformado, enum fora da lista ou valor de tipo errado no corpo. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> corpoIlegivel(HttpMessageNotReadableException ex,
			HttpServletRequest requisicao) {
		if (ex.getMostSpecificCause() instanceof JacksonException jackson && !jackson.getPath().isEmpty()) {
			String campo = jackson.getPath().stream()
					.map(referencia -> referencia.getPropertyName() != null ? referencia.getPropertyName()
							: "[" + referencia.getIndex() + "]")
					.collect(Collectors.joining("."));
			return responder(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição.",
					List.of(new CampoInvalido(campo, valorAceito(jackson))), requisicao);
		}
		return responder(HttpStatus.BAD_REQUEST, "O corpo da requisição não é um JSON válido.", List.of(),
				requisicao);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErroResponse> parametroDeTipoErrado(MethodArgumentTypeMismatchException ex,
			HttpServletRequest requisicao) {
		return responder(HttpStatus.BAD_REQUEST,
				"O valor '" + ex.getValue() + "' não é válido para '" + ex.getName() + "'.", List.of(), requisicao);
	}

	/** Restrição do banco que escapou do service: a FK ou o índice único são a rede de segurança. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResponse> violacaoDeIntegridade(DataIntegrityViolationException ex,
			HttpServletRequest requisicao) {
		log.warn("Restrição do banco violada em {}", requisicao.getRequestURI(), ex);
		return responder(HttpStatus.CONFLICT,
				"A operação conflita com um registro existente: dado duplicado ou registro ainda em uso.", List.of(),
				requisicao);
	}

	/** Erros do próprio Spring MVC: rota inexistente, método não suportado, tipo de conteúdo errado... */
	@ExceptionHandler(ServletException.class)
	public ResponseEntity<ErroResponse> erroDoSpring(ServletException ex, HttpServletRequest requisicao) {
		if (ex instanceof ErrorResponse resposta && resposta.getStatusCode().is4xxClientError()) {
			HttpStatusCode status = resposta.getStatusCode();
			return responder(status, mensagemPadrao(status), List.of(), requisicao);
		}
		return inesperado(ex, requisicao);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest requisicao) {
		log.error("Erro inesperado em {}", requisicao.getRequestURI(), ex);
		return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado no servidor. Tente novamente.",
				List.of(), requisicao);
	}

	private static String valorAceito(JacksonException ex) {
		if (ex instanceof InvalidFormatException formato && formato.getTargetType() != null
				&& formato.getTargetType().isEnum()) {
			String aceitos = Arrays.stream(formato.getTargetType().getEnumConstants()).map(Object::toString)
					.collect(Collectors.joining(", "));
			return "valor '" + formato.getValue() + "' fora da lista; use um de: " + aceitos;
		}
		return "valor com tipo ou formato inválido";
	}

	private static String mensagemPadrao(HttpStatusCode status) {
		return switch (status.value()) {
			case 404 -> "Rota não encontrada.";
			case 405 -> "Método HTTP não suportado nesta rota.";
			case 406 -> "Formato de resposta não suportado; a API responde em JSON.";
			case 415 -> "Tipo de conteúdo não suportado; envie o corpo em JSON (application/json).";
			default -> "Requisição inválida.";
		};
	}

	private static String descricao(HttpStatusCode status) {
		return switch (status.value()) {
			case 400 -> "Requisição inválida";
			case 404 -> "Não encontrado";
			case 405 -> "Método não permitido";
			case 406 -> "Formato não aceito";
			case 409 -> "Conflito";
			case 415 -> "Tipo de conteúdo não suportado";
			case 500 -> "Erro interno";
			default -> status.is4xxClientError() ? "Requisição inválida" : "Erro interno";
		};
	}

	private static ResponseEntity<ErroResponse> responder(HttpStatusCode status, String mensagem,
			List<CampoInvalido> campos, HttpServletRequest requisicao) {
		ErroResponse corpo = new ErroResponse(Instant.now(), status.value(), descricao(status), mensagem,
				requisicao.getRequestURI(), campos);
		return ResponseEntity.status(status).body(corpo);
	}

}
