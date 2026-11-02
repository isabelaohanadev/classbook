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

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.DisciplinaResponse;
import br.com.classbook.service.DisciplinaService;

@Tag(name = "Disciplinas", description = "Disciplinas do ensino médio, às quais os materiais digitais se vinculam")
@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

	private final DisciplinaService disciplinaService;

	public DisciplinaController(DisciplinaService disciplinaService) {
		this.disciplinaService = disciplinaService;
	}

	@PostMapping
	@Operation(summary = "Cadastra uma disciplina", description = "O código é gravado em maiúsculas e sem espaços "
			+ "nas pontas.")
	@ApiResponse(responseCode = "201", description = "Disciplina cadastrada; o header Location aponta para ela")
	@ApiResponse(responseCode = "400", description = "Campo inválido, série fora da lista ou JSON malformado")
	@ApiResponse(responseCode = "409", description = "Já existe uma disciplina com esse código")
	public ResponseEntity<DisciplinaResponse> criar(@Valid @RequestBody DisciplinaRequest request) {
		DisciplinaResponse disciplina = disciplinaService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(disciplina.id()).toUri();
		return ResponseEntity.created(location).body(disciplina);
	}

	@GetMapping
	@Operation(summary = "Lista as disciplinas")
	@ApiResponse(responseCode = "200", description = "Disciplinas ordenadas por código")
	public List<DisciplinaResponse> listar() {
		return disciplinaService.listar();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma disciplina pelo ID")
	@ApiResponse(responseCode = "200", description = "Disciplina encontrada")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Disciplina não encontrada")
	public DisciplinaResponse buscarPorId(@PathVariable Long id) {
		return disciplinaService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edita uma disciplina", description = "O código é gravado em maiúsculas e sem espaços nas "
			+ "pontas.")
	@ApiResponse(responseCode = "200", description = "Disciplina editada")
	@ApiResponse(responseCode = "400", description = "Campo inválido, série fora da lista, ID que não é número ou "
			+ "JSON malformado")
	@ApiResponse(responseCode = "404", description = "Disciplina não encontrada")
	@ApiResponse(responseCode = "409", description = "Outra disciplina já usa esse código")
	public DisciplinaResponse atualizar(@PathVariable Long id, @Valid @RequestBody DisciplinaRequest request) {
		return disciplinaService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui uma disciplina", description = "Só é possível depois de excluir os materiais "
			+ "digitais vinculados a ela.")
	@ApiResponse(responseCode = "204", description = "Disciplina excluída")
	@ApiResponse(responseCode = "400", description = "ID que não é número")
	@ApiResponse(responseCode = "404", description = "Disciplina não encontrada")
	@ApiResponse(responseCode = "409", description = "A disciplina tem materiais digitais vinculados")
	public void excluir(@PathVariable Long id) {
		disciplinaService.excluir(id);
	}

}
