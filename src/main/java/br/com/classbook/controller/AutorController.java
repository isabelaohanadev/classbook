package br.com.classbook.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.classbook.dto.AutorRequest;
import br.com.classbook.dto.AutorResponse;
import br.com.classbook.service.AutorService;

@Tag(name = "Autores", description = "Autores dos livros do acervo")
@RestController
@RequestMapping("/api/autores")
public class AutorController {

	private final AutorService autorService;

	public AutorController(AutorService autorService) {
		this.autorService = autorService;
	}

	@PostMapping
	@Operation(summary = "Cadastra um autor")
	@ApiResponse(responseCode = "201", description = "Autor cadastrado; o header Location aponta para ele")
	@ApiResponse(responseCode = "400", description = "Campo inválido ou JSON malformado")
	public ResponseEntity<AutorResponse> criar(@Valid @RequestBody AutorRequest request) {
		AutorResponse autor = autorService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(autor.id()).toUri();
		return ResponseEntity.created(location).body(autor);
	}

	@GetMapping
	@Operation(summary = "Lista os autores")
	@ApiResponse(responseCode = "200", description = "Autores ordenados por nome")
	public List<AutorResponse> listar() {
		return autorService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um autor pelo ID")
	@ApiResponse(responseCode = "200", description = "Autor encontrado")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Autor não encontrado")
	public AutorResponse buscarPorId(@PathVariable Long id) {
		return autorService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edita um autor")
	@ApiResponse(responseCode = "200", description = "Autor editado")
	@ApiResponse(responseCode = "400", description = "Campo inválido, ID que não é número ou JSON malformado")
	@ApiResponse(responseCode = "404", description = "Autor não encontrado")
	public AutorResponse atualizar(@PathVariable Long id, @Valid @RequestBody AutorRequest request) {
		return autorService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui um autor")
	@ApiResponse(responseCode = "204", description = "Autor excluído")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Autor não encontrado")
	@ApiResponse(responseCode = "409", description = "Autor possui livros vinculados")
	public void excluir(@PathVariable Long id) {
		autorService.excluir(id);
	}

}
