package br.com.classbook.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

@Tag(name = "Livros", description = "Livros do acervo, com a pesquisa avançada por título, autor e categoria")
@RestController
@RequestMapping("/api/livros")
public class LivroController {

	private final LivroService livroService;

	public LivroController(LivroService livroService) {
		this.livroService = livroService;
	}

	@PostMapping
	@Operation(summary = "Cadastra um livro")
	@ApiResponse(responseCode = "201", description = "Livro cadastrado; o header Location aponta para ele")
	@ApiResponse(responseCode = "400",
			description = "autorId ausente ou inexistente, categoriaId informado e inexistente, "
					+ "ou demais campos inválidos")
	@ApiResponse(responseCode = "409", description = "ISBN já cadastrado em outro livro")
	public ResponseEntity<LivroResponse> criar(@Valid @RequestBody LivroRequest request) {
		LivroResponse livro = livroService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(livro.id()).toUri();
		return ResponseEntity.created(location).body(livro);
	}

	@GetMapping
	@Operation(summary = "Pesquisa os livros",
			description = "Filtros opcionais e combináveis (E lógico): titulo (contém, sem diferenciar "
					+ "maiúsculas), autorId e categoriaId. Sem filtros, devolve todos os livros.")
	@ApiResponse(responseCode = "200", description = "Livros ordenados por título; lista vazia sem correspondência")
	@ApiResponse(responseCode = "400", description = "autorId ou categoriaId que não é número")
	public List<LivroResponse> listar(
			@Parameter(description = "Contém o texto, sem diferenciar maiúsculas de minúsculas")
			@RequestParam(required = false) String titulo,
			@Parameter(description = "ID do autor; inexistente responde lista vazia, não erro")
			@RequestParam(required = false) Long autorId,
			@Parameter(description = "ID da categoria; inexistente responde lista vazia, não erro")
			@RequestParam(required = false) Long categoriaId) {
		return livroService.listar(titulo, autorId, categoriaId);
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
			description = "autorId ausente ou inexistente, categoriaId informado e inexistente, ID que não "
					+ "é número, ou demais campos inválidos")
	@ApiResponse(responseCode = "404", description = "Livro não encontrado")
	@ApiResponse(responseCode = "409", description = "ISBN já cadastrado em outro livro")
	public LivroResponse atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequest request) {
		return livroService.atualizar(id, request);
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
