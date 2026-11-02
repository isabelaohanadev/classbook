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

import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.dto.MaterialDigitalResponse;
import br.com.classbook.service.MaterialDigitalService;

@RestController
@RequestMapping("/api/materiais")
public class MaterialDigitalController {

	private final MaterialDigitalService materialDigitalService;

	public MaterialDigitalController(MaterialDigitalService materialDigitalService) {
		this.materialDigitalService = materialDigitalService;
	}

	@PostMapping
	public ResponseEntity<MaterialDigitalResponse> criar(@Valid @RequestBody MaterialDigitalRequest request) {
		MaterialDigitalResponse material = materialDigitalService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(material.id()).toUri();
		return ResponseEntity.created(location).body(material);
	}

	@GetMapping
	public List<MaterialDigitalResponse> listar() {
		return materialDigitalService.listar();
	}

	@GetMapping("/{id}")
	public MaterialDigitalResponse buscarPorId(@PathVariable Long id) {
		return materialDigitalService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public MaterialDigitalResponse atualizar(@PathVariable Long id,
			@Valid @RequestBody MaterialDigitalRequest request) {
		return materialDigitalService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		materialDigitalService.excluir(id);
	}

}
