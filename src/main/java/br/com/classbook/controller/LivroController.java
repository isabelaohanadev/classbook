package br.com.classbook.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import tools.jackson.databind.JsonNode;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.classbook.dto.LivroRequest;
import br.com.classbook.dto.LivroResponse;
import br.com.classbook.service.LivroService;

@Tag(name = "Livros",
		description = "Livros do acervo, com a busca livre (título, autor, categoria ou ano) e os filtros por título, autor e categoria")
@RestController
@RequestMapping("/api/livros")
public class LivroController {

	private static final String EXEMPLO_PATCH = "{\"editora\": \"Nova Editora\"}";

	private final LivroService livroService;

	public LivroController(LivroService livroService) {
		this.livroService = livroService;
	}

	@PostMapping
	@Operation(summary = "Cadastra um livro")
	@ApiResponse(responseCode = "201", description = "Livro cadastrado; o header Location aponta para ele")
	@ApiResponse(responseCode = "400",
			description = "autorId ausente ou inexistente, categoriaId ou disciplinaId informado e "
					+ "inexistente, ou demais campos inválidos")
	@ApiResponse(responseCode = "409", description = "ISBN já cadastrado em outro livro")
	public ResponseEntity<LivroResponse> criar(@Valid @RequestBody LivroRequest request) {
		LivroResponse livro = livroService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(livro.id()).toUri();
		return ResponseEntity.created(location).body(livro);
	}

	@GetMapping
	@Operation(summary = "Pesquisa os livros",
			description = "Filtros opcionais e combináveis (E lógico): busca (texto livre, que procura no "
					+ "título, no nome do autor, no nome da categoria e no ano de publicação), titulo (contém, "
					+ "sem diferenciar maiúsculas), autorId e categoriaId. Sem filtros, devolve todos os livros.")
	@ApiResponse(responseCode = "200", description = "Livros ordenados por título; lista vazia sem correspondência")
	@ApiResponse(responseCode = "400", description = "autorId ou categoriaId que não é número")
	public List<LivroResponse> listar(
			@Parameter(description = "Texto livre: o livro entra se o texto estiver no título, no nome do autor, "
					+ "no nome da categoria ou no ano de publicação (parcial, sem diferenciar maiúsculas de "
					+ "minúsculas; por exemplo, 189 acha 1899)")
			@RequestParam(required = false) String busca,
			@Parameter(description = "Contém o texto no título, sem diferenciar maiúsculas de minúsculas")
			@RequestParam(required = false) String titulo,
			@Parameter(description = "ID do autor; inexistente responde lista vazia, não erro")
			@RequestParam(required = false) Long autorId,
			@Parameter(description = "ID da categoria; inexistente responde lista vazia, não erro")
			@RequestParam(required = false) Long categoriaId) {
		return livroService.listar(titulo, busca, autorId, categoriaId);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um livro pelo ID")
	@ApiResponse(responseCode = "200", description = "Livro encontrado")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	public LivroResponse buscarPorId(@PathVariable Long id) {
		return livroService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edita um livro")
	@ApiResponse(responseCode = "200", description = "Livro editado")
	@ApiResponse(responseCode = "400",
			description = "autorId ausente ou inexistente, categoriaId ou disciplinaId informado e "
					+ "inexistente, ID que não é número, ou demais campos inválidos")
	@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	@ApiResponse(responseCode = "409", description = "ISBN já cadastrado em outro livro")
	public LivroResponse atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequest request) {
		return livroService.atualizar(id, request);
	}

	@PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE, MergePatch.CONTENT_TYPE })
	@Operation(summary = "Edita parcialmente um livro", description = MergePatch.REGRAS
			+ " Campos aceitos: titulo, isbn, anoPublicacao, descricao, editora, autorId, categoriaId e "
			+ "disciplinaId. Com categoriaId ou disciplinaId null, o livro sai da categoria ou da disciplina.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
			@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"),
					examples = @ExampleObject(value = EXEMPLO_PATCH)),
			@Content(mediaType = MergePatch.CONTENT_TYPE, schema = @Schema(type = "object"),
					examples = @ExampleObject(value = EXEMPLO_PATCH)) })
	@ApiResponse(responseCode = "200", description = "Livro editado")
	@ApiResponse(responseCode = "400",
			description = "autorId inexistente ou null, categoriaId ou disciplinaId informado e inexistente, ID "
					+ "que não é número, corpo que não é um objeto JSON, JSON malformado ou demais campos inválidos")
	@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	@ApiResponse(responseCode = "409", description = "ISBN já cadastrado em outro livro")
	public LivroResponse atualizarParcialmente(@PathVariable Long id, @RequestBody JsonNode alteracoes) {
		return livroService.atualizarParcialmente(id, alteracoes);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui um livro")
	@ApiResponse(responseCode = "204", description = "Livro excluído")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	public void excluir(@PathVariable Long id) {
		livroService.excluir(id);
	}

}
