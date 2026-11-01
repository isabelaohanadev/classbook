package br.com.classbook.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

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

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@PostMapping
	public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
		CategoriaResponse categoria = categoriaService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(categoria.id()).toUri();
		return ResponseEntity.created(location).body(categoria);
	}

	@GetMapping
	public List<CategoriaResponse> listar() {
		return categoriaService.listar();
	}

	@GetMapping("/{id}")
	public CategoriaResponse buscarPorId(@PathVariable Long id) {
		return categoriaService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
		return categoriaService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		categoriaService.excluir(id);
	}

}
