package br.com.classbook.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.classbook.service.MaterialDigitalService;

@RestController
@RequestMapping("/api/materiais")
public class MaterialDigitalController {

	private final MaterialDigitalService materialDigitalService;

	public MaterialDigitalController(MaterialDigitalService materialDigitalService) {
		this.materialDigitalService = materialDigitalService;
	}

}
