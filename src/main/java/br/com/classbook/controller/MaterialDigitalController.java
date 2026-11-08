package br.com.classbook.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.dto.MaterialDigitalResponse;
import br.com.classbook.service.MaterialDigitalService;

@Tag(name = "Materiais digitais", description = "Links para PDFs, vídeos e outros materiais, cada um de uma "
		+ "disciplina")
@RestController
@RequestMapping("/api/materiais")
public class MaterialDigitalController {

	private static final String EXEMPLO_PATCH = "{\"url\": \"https://exemplo.com/novo-link.pdf\"}";

	private final MaterialDigitalService materialDigitalService;

	public MaterialDigitalController(MaterialDigitalService materialDigitalService) {
		this.materialDigitalService = materialDigitalService;
	}

	@PostMapping
	@Operation(summary = "Cadastra um material digital")
	@ApiResponse(responseCode = "201", description = "Material cadastrado, com a disciplina resumida; o header "
			+ "Location aponta para ele")
	@ApiResponse(responseCode = "400", description = "Campo inválido, URL que não é http ou https, formato fora da "
			+ "lista, disciplina inexistente ou JSON malformado")
	public ResponseEntity<MaterialDigitalResponse> criar(@Valid @RequestBody MaterialDigitalRequest request) {
		MaterialDigitalResponse material = materialDigitalService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(material.id()).toUri();
		return ResponseEntity.created(location).body(material);
	}

	@GetMapping
	@Operation(summary = "Lista os materiais digitais")
	@ApiResponse(responseCode = "200", description = "Materiais ordenados por título, cada um com a disciplina "
			+ "resumida")
	public List<MaterialDigitalResponse> listar() {
		return materialDigitalService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um material digital pelo ID")
	@ApiResponse(responseCode = "200", description = "Material encontrado, com a disciplina resumida")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Material não encontrado")
	public MaterialDigitalResponse buscarPorId(@PathVariable Long id) {
		return materialDigitalService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edita um material digital", description = "Também serve para trocar a disciplina do "
			+ "material.")
	@ApiResponse(responseCode = "200", description = "Material editado, com a disciplina resumida")
	@ApiResponse(responseCode = "400", description = "Campo inválido, URL que não é http ou https, formato fora da "
			+ "lista, disciplina inexistente, ID que não é número ou JSON malformado")
	@ApiResponse(responseCode = "404", description = "Material não encontrado")
	public MaterialDigitalResponse atualizar(@PathVariable Long id,
			@Valid @RequestBody MaterialDigitalRequest request) {
		return materialDigitalService.atualizar(id, request);
	}

	@PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE, MergePatch.CONTENT_TYPE })
	@Operation(summary = "Edita parcialmente um material digital", description = MergePatch.REGRAS
			+ " Campos aceitos: titulo, formato, url, descricao, dataPublicacao e disciplinaId.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
			@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"),
					examples = @ExampleObject(value = EXEMPLO_PATCH)),
			@Content(mediaType = MergePatch.CONTENT_TYPE, schema = @Schema(type = "object"),
					examples = @ExampleObject(value = EXEMPLO_PATCH)) })
	@ApiResponse(responseCode = "200", description = "Material editado, com a disciplina resumida")
	@ApiResponse(responseCode = "400", description = "Campo inválido, URL que não é http ou https, formato fora da "
			+ "lista, disciplina inexistente, ID que não é número, corpo que não é um objeto JSON ou JSON malformado")
	@ApiResponse(responseCode = "404", description = "Material não encontrado")
	public MaterialDigitalResponse atualizarParcialmente(@PathVariable Long id, @RequestBody JsonNode alteracoes) {
		return materialDigitalService.atualizarParcialmente(id, alteracoes);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui um material digital", description = "A disciplina do material continua cadastrada.")
	@ApiResponse(responseCode = "204", description = "Material excluído")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Material não encontrado")
	public void excluir(@PathVariable Long id) {
		materialDigitalService.excluir(id);
	}

}
