package br.com.classbook.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.classbook.service.DisciplinaService;

@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

	private final DisciplinaService disciplinaService;

	public DisciplinaController(DisciplinaService disciplinaService) {
		this.disciplinaService = disciplinaService;
	}

}
