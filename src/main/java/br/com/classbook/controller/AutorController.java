package br.com.classbook.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

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

@RestController
@RequestMapping("/api/autores")
public class AutorController {

	private final AutorService autorService;

	public AutorController(AutorService autorService) {
		this.autorService = autorService;
	}

	@PostMapping
	public ResponseEntity<AutorResponse> criar(@Valid @RequestBody AutorRequest request) {
		AutorResponse autor = autorService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(autor.id()).toUri();
		return ResponseEntity.created(location).body(autor);
	}

	@GetMapping
	public List<AutorResponse> listar() {
		return autorService.listar();
	}

	@GetMapping("/{id}")
	public AutorResponse buscarPorId(@PathVariable Long id) {
		return autorService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public AutorResponse atualizar(@PathVariable Long id, @Valid @RequestBody AutorRequest request) {
		return autorService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		autorService.excluir(id);
	}

}
