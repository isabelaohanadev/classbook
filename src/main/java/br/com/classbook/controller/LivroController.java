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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.classbook.dto.LivroRequest;
import br.com.classbook.dto.LivroResponse;
import br.com.classbook.service.LivroService;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

	private final LivroService livroService;

	public LivroController(LivroService livroService) {
		this.livroService = livroService;
	}

	@PostMapping
	public ResponseEntity<LivroResponse> criar(@Valid @RequestBody LivroRequest request) {
		LivroResponse livro = livroService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(livro.id()).toUri();
		return ResponseEntity.created(location).body(livro);
	}

	@GetMapping
	public List<LivroResponse> listar(@RequestParam(required = false) String titulo) {
		return livroService.listar(titulo);
	}

	@GetMapping("/{id}")
	public LivroResponse buscarPorId(@PathVariable Long id) {
		return livroService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public LivroResponse atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequest request) {
		return livroService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		livroService.excluir(id);
	}

}
