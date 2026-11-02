package br.com.classbook.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.dto.CategoriaResponse;
import br.com.classbook.service.CategoriaService;

@Tag(name = "Categorias", description = "Categorias de livros, como didático, paradidático e literatura")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@PostMapping
	@Operation(summary = "Cadastra uma categoria")
	@ApiResponse(responseCode = "201", description = "Categoria cadastrada; o header Location aponta para ela")
	@ApiResponse(responseCode = "400", description = "Campo inválido, tipo fora da lista ou JSON malformado")
	@ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome, sem diferenciar "
			+ "maiúsculas")
	public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
		CategoriaResponse categoria = categoriaService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(categoria.id()).toUri();
		return ResponseEntity.created(location).body(categoria);
	}

	@GetMapping
	@Operation(summary = "Lista as categorias")
	@ApiResponse(responseCode = "200", description = "Categorias ordenadas por nome")
	public List<CategoriaResponse> listar() {
		return categoriaService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma categoria pelo ID")
	@ApiResponse(responseCode = "200", description = "Categoria encontrada")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Categoria não encontrada")
	public CategoriaResponse buscarPorId(@PathVariable Long id) {
		return categoriaService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edita uma categoria")
	@ApiResponse(responseCode = "200", description = "Categoria editada")
	@ApiResponse(responseCode = "400", description = "Campo inválido, tipo fora da lista, ID que não é número ou "
			+ "JSON malformado")
	@ApiResponse(responseCode = "404", description = "Categoria não encontrada")
	@ApiResponse(responseCode = "409", description = "Outra categoria já usa esse nome, sem diferenciar maiúsculas")
	public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
		return categoriaService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui uma categoria")
	@ApiResponse(responseCode = "204", description = "Categoria excluída")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Categoria não encontrada")
	@ApiResponse(responseCode = "409", description = "Categoria possui livros vinculados")
	public void excluir(@PathVariable Long id) {
		categoriaService.excluir(id);
	}

}
